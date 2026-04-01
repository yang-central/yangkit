package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.impl.stmt.type.RequireInstanceImpl;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
import org.yangcentral.yangkit.xpath.impl.YangLocationPathImpl;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Step;
import org.jaxen.saxpath.Axis;
import org.yangcentral.yangkit.model.api.stmt.ModelException;

import java.net.URI;
import java.util.List;

public class InstanceIdentifierImpl extends RestrictionImpl<YangAbsoluteLocationPath> implements InstanceIdentifier {
   private enum PathValidationStatus {
      INVALID,
      RESOLVED,
      UNRESOLVED
   }

   private RequireInstance requireInstance;

   public InstanceIdentifierImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public InstanceIdentifierImpl(YangContext context) {
      super(context);
   }

   public RequireInstance getRequireInstance() {
      return this.requireInstance;
   }

   public void setRequireInstance(RequireInstance requireInstance) {
      this.requireInstance = requireInstance;
   }

   public boolean isRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (null != requireInstance) {
         return requireInstance.value();
      } else if (this.getDerived() != null) {
         InstanceIdentifier instanceIdentifier = (InstanceIdentifier)this.getDerived().getType().getRestriction();
         return instanceIdentifier.isRequireInstance();
      } else {
         return true;
      }
   }

   public RequireInstance getEffectiveRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (requireInstance != null) {
         return requireInstance;
      } else if (this.getDerived() != null) {
         InstanceIdentifier instanceIdentifier = (InstanceIdentifier)this.getDerived().getType().getRestriction();
         return instanceIdentifier.getEffectiveRequireInstance();
      } else {
         RequireInstance newRequireInstance = new RequireInstanceImpl("true");
         newRequireInstance.setContext(new YangContext(this.getContext()));
         newRequireInstance.setElementPosition(this.getContext().getSelf().getElementPosition());
         newRequireInstance.setParentStatement(this.getContext().getSelf());
         newRequireInstance.init();
         newRequireInstance.build();
         return newRequireInstance;
      }
   }

   private SchemaNode getDefineNode() {
      YangContext context = this.getContext();
      if (context == null || context.getSelf() == null) {
         return null;
      }

      YangStatement statement = context.getSelf();
      while (statement != null) {
         if (statement instanceof SchemaNode) {
            return (SchemaNode) statement;
         }
         statement = statement.getParentStatement();
      }
      return null;
   }

   private QName resolveStepQName(NameStep step, Object currentNode) {
      try {
         String prefix = step.getPrefix();
         String localName = step.getLocalName();
         URI namespace;
         if (prefix != null && !prefix.isEmpty()) {
            Module module = ModelUtil.findModuleByPrefix(this.getContext(), prefix);
            namespace = module.getMainModule().getNamespace().getUri();
         } else if (currentNode instanceof SchemaNode) {
            SchemaNode schemaNode = (SchemaNode) currentNode;
            namespace = schemaNode.getIdentifier().getNamespace();
            prefix = schemaNode.getIdentifier().getPrefix();
         } else {
            Module curModule = this.getContext().getCurModule();
            namespace = curModule.getMainModule().getNamespace().getUri();
            prefix = curModule.getSelfPrefix();
         }
         return new QName(namespace, prefix, localName);
      } catch (ModelException e) {
         return null;
      }
   }

    private boolean isValidListPredicate(YangList list, Predicate predicate) {
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

      QName keyQName = resolveStepQName(keyStep, list);
      return keyQName != null && list.getKey().getKeyNode(keyQName) != null;
   }

   private PathValidationStatus validatePath(YangAbsoluteLocationPath value) {
      YangSchemaContext schemaContext = this.getContext().getSchemaContext();
      if (schemaContext == null) {
         return PathValidationStatus.INVALID;
      }

      Object current = schemaContext;
      boolean unresolved = false;
      for (Step step : ((YangLocationPathImpl) value).getSteps()) {
         if (!(step instanceof NameStep) || step.getAxis() != Axis.CHILD) {
            return PathValidationStatus.INVALID;
         }

         NameStep nameStep = (NameStep) step;
         if (resolveStepQName(nameStep, current) == null) {
            return PathValidationStatus.INVALID;
         }

         if (unresolved) {
            if (!step.getPredicates().isEmpty()) {
               return PathValidationStatus.INVALID;
            }
            continue;
         }

         if (!(current instanceof SchemaNodeContainer)) {
            return PathValidationStatus.INVALID;
         }

         QName childQName = resolveStepQName(nameStep, current);
         SchemaNode child = ((SchemaNodeContainer) current).getTreeNodeChild(childQName);
         if (child == null) {
            if (!step.getPredicates().isEmpty()) {
               return PathValidationStatus.INVALID;
            }
            unresolved = true;
            current = null;
            continue;
         }

         if (child instanceof YangList) {
            YangList list = (YangList) child;
            List<?> predicates = step.getPredicates();
            if (list.getKey() != null && list.getKey().getkeyNodes().size() > predicates.size()) {
               return PathValidationStatus.INVALID;
            }
            for (Object predicateObj : predicates) {
               if (!(predicateObj instanceof Predicate)
                       || !isValidListPredicate(list, (Predicate) predicateObj)) {
                  return PathValidationStatus.INVALID;
               }
            }
         } else if (!step.getPredicates().isEmpty()) {
            return PathValidationStatus.INVALID;
         }

         current = child;
      }

      return unresolved ? PathValidationStatus.UNRESOLVED : PathValidationStatus.RESOLVED;
   }

   public boolean evaluate(YangAbsoluteLocationPath value) {
      if (value == null || !value.isAbsolute() || !(value instanceof YangLocationPathImpl)) {
         return false;
      }

      SchemaNode defineNode = getDefineNode();
      if (defineNode == null) {
         return false;
      }
      PathValidationStatus status = validatePath(value);
      if (status == PathValidationStatus.INVALID) {
         return false;
      }
      return status == PathValidationStatus.RESOLVED || !this.isRequireInstance();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof InstanceIdentifier)) {
         return false;
      } else {
         InstanceIdentifier another = (InstanceIdentifier)obj;
         return this.isRequireInstance() == another.isRequireInstance();
      }
   }
}
