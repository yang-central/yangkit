package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Step;
import org.jaxen.saxpath.Axis;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import java.util.Collections;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.util.SingletonList;
import org.yangcentral.yangkit.xpath.YangContextSupport;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;

public class YangAbsoluteLocationPathImpl extends YangLocationPathImpl implements YangAbsoluteLocationPath {
   private Module findModuleByPrefix(YangSchemaContext schemaContext, String prefix) {
      for (Module module : schemaContext.getModules()) {
         if (module.isSelfPrefix(prefix)) {
            return module;
         }
      }
      for (Module module : schemaContext.getImportOnlyModules()) {
         if (module.isSelfPrefix(prefix)) {
            return module;
         }
      }
      return null;
   }

   private QName resolveStepQName(YangSchemaContext schemaContext, NameStep nameStep) {
      String prefix = nameStep.getPrefix();
      if (prefix == null || prefix.isEmpty()) {
         return null;
      }

      Module module = findModuleByPrefix(schemaContext, prefix);
      if (module == null) {
         return null;
      }

      return new QName(module.getMainModule().getNamespace().getUri(), prefix, nameStep.getLocalName());
   }

   private boolean isValidListPredicate(YangSchemaContext schemaContext, YangList list, Predicate predicate) {
      if (!(predicate.getExpr() instanceof EqualityExpr) || list.getKey() == null) {
         return false;
      }

      Expr lhs = ((EqualityExpr) predicate.getExpr()).getLHS();
      if (!(lhs instanceof LocationPath)) {
         return false;
      }

      List<?> steps = ((LocationPath) lhs).getSteps();
      if (steps.size() != 1 || !(steps.get(0) instanceof NameStep)) {
         return false;
      }

      NameStep keyStep = (NameStep) steps.get(0);
      if (keyStep.getAxis() != Axis.CHILD) {
         return false;
      }

      QName keyQName = resolveStepQName(schemaContext, keyStep);
      return keyQName != null && list.getKey().getKeyNode(keyQName) != null;
   }

   public boolean isAbsolute() {
      return true;
   }

   public String getText() {
      return "/" + super.getText();
   }

   public Object evaluate(Context context) throws JaxenException {
      YangContextSupport support = (YangContextSupport)context.getContextSupport();
      Navigator nav = support.getNavigator();
      Context absContext = new Context(support);
      List<?> contextNodes = context.getNodeSet();
      if (contextNodes.isEmpty()) {
         return Collections.EMPTY_LIST;
      } else {
         Object firstNode = contextNodes.get(0);
         Object docNode = nav.getDocumentNode(firstNode);
         if (docNode == null) {
            return Collections.EMPTY_LIST;
         } else {
            List<?> list = new SingletonList(docNode);
            absContext.setNodeSet(list);
            return super.evaluate(absContext);
         }
      }
   }

   public String toString() {
      return "[(YangAbsoluteLocationPath): " + super.toString() + "]";
   }

   public boolean isInstanceIdentifier(YangSchemaContext schemaContext) {
      if (schemaContext == null || this.getSteps().isEmpty()) {
         return false;
      }

      Object current = schemaContext;
      for (Step step : this.getSteps()) {
         if (!(step instanceof NameStep) || step.getAxis() != Axis.CHILD || !(current instanceof SchemaNodeContainer)) {
            return false;
         }

         NameStep nameStep = (NameStep) step;
         QName stepQName = resolveStepQName(schemaContext, nameStep);
         if (stepQName == null) {
            return false;
         }

         SchemaNode child = ((SchemaNodeContainer) current).getTreeNodeChild(stepQName);
         if (child == null) {
            return false;
         }

         if (child instanceof YangList) {
            YangList list = (YangList) child;
            List<Predicate> predicates = step.getPredicates();
            if (list.getKey() != null && list.getKey().getkeyNodes().size() > predicates.size()) {
               return false;
            }
            for (Predicate predicate : predicates) {
               if (!isValidListPredicate(schemaContext, list, predicate)) {
                  return false;
               }
            }
         } else if (!step.getPredicates().isEmpty()) {
            return false;
         }

         current = child;
      }

      return current instanceof SchemaNode;
   }
}
