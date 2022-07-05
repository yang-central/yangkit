package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Step;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangContextSupport;
import org.yangcentral.yangkit.xpath.YangLocationPath;

import java.net.URI;
import java.util.*;

public abstract class YangLocationPathImpl implements YangLocationPath {
   private List<Step> steps = new LinkedList();

   public void addStep(Step step) {
      this.steps.add(step);
   }

   public List getSteps() {
      return this.steps;
   }

   public String getText() {
      StringBuffer buf = new StringBuffer();
      Iterator stepIter = this.getSteps().iterator();

      while(stepIter.hasNext()) {
         Step step = (Step)stepIter.next();
         if (step.getAxis() == 3) {
            buf.append("..");
         } else if (step.getAxis() == 11) {
            buf.append(".");
         } else {
            buf.append(step.getText());
         }

         if (stepIter.hasNext()) {
            buf.append("/");
         }
      }

      return buf.toString();
   }

   public Expr simplify() {
      Iterator stepIter = this.getSteps().iterator();
      Step eachStep = null;

      while(stepIter.hasNext()) {
         eachStep = (Step)stepIter.next();
         eachStep.simplify();
      }

      return this;
   }

   public static SchemaNodeContainer getXPathSchemaParent(SchemaNode current) {
      SchemaNodeContainer parent = current.getParentSchemaNode();
      if (parent == null) {
         current.getParentSchemaNode();
      }

      if (!(parent instanceof VirtualSchemaNode) && !(parent instanceof Case) && !(parent instanceof Choice) && !(parent instanceof Input) && !(parent instanceof Output)) {
         if (parent instanceof Module) {
            return new YangXPathRoot((Module)parent);
         } else {
            return (SchemaNodeContainer)(parent.isSchemaTreeRoot() ? new YangXPathRoot((SchemaNode)parent) : parent);
         }
      } else {
         return getXPathSchemaParent((SchemaNode)parent);
      }
   }

   public static YangXPathRoot getXPathSchemaRoot(SchemaNode context) {
      SchemaNodeContainer schemaRoot = context.getSchemaTreeRoot();
      return schemaRoot instanceof Module ? new YangXPathRoot((Module)schemaRoot) : new YangXPathRoot((SchemaNode)schemaRoot);
   }

   public static YangXPathRoot getXPathSchemaRoot(Module context) {
      return new YangXPathRoot(context);
   }

   public static SchemaNode getXPathSchemaChild(SchemaNodeContainer parent, QName child) {
      SchemaNode childNode = null;
      childNode = parent.getDataNodeChild(child);
      return childNode;
   }

   public SchemaNode getTargetSchemaNode(YangXPathContext xPathContext) throws ModelException {
      List steps = this.getSteps();
      Object current = xPathContext.getCurrentNode();
      if (this.isAbsolute()) {
         if (current instanceof Module) {
            current = new YangXPathRoot((Module)current);
         } else {
            current = new YangXPathRoot((SchemaNode)current);
         }
      }

      Iterator var4 = steps.iterator();

      while(true) {
         while(var4.hasNext()) {
            Object o = var4.next();
            Step step = (Step)o;
            if (step.getAxis() == 3) {
               if (current instanceof SchemaNodeContainer && ((SchemaNodeContainer)current).isSchemaTreeRoot()) {
                  throw new ModelException(Severity.ERROR, xPathContext.getDefineNode(), ErrorCode.INVALID_XPATH.getFieldName());
               }

               SchemaNodeContainer parent = getXPathSchemaParent((SchemaNode)current);
               current = parent;
            } else if (step.getAxis() == 1) {
               NameStep nameStep = (NameStep)step;
               String prefix = nameStep.getPrefix();
               String localName = nameStep.getLocalName();
               URI namespace = null;
               if (prefix != null && prefix.length() > 0) {
                  Module module = ModelUtil.findModuleByPrefix(xPathContext.getYangContext(), prefix);
                  if (null == module) {
                     throw new ModelException(Severity.ERROR, xPathContext.getDefineNode(), ErrorCode.INVALID_PREFIX.toString(new String[]{"name=" + prefix}));
                  }

                  namespace = module.getMainModule().getNamespace().getUri();
               } else {
                  Object contextNode = xPathContext.getContextNode();
                  if (contextNode instanceof Module) {
                     Module curModule = (Module)contextNode;
                     namespace = curModule.getMainModule().getNamespace().getUri();
                     prefix = curModule.getSelfPrefix();
                  } else {
                     namespace = ((SchemaNode)contextNode).getIdentifier().getNamespace();
                     prefix = ((SchemaNode)contextNode).getIdentifier().getPrefix();
                  }
               }

               QName childQName = new QName(namespace, prefix, localName);
               if (!(current instanceof SchemaNodeContainer)) {
                  throw new ModelException(Severity.ERROR, xPathContext.getDefineNode(), ErrorCode.INVALID_XPATH_TERMIANL_HAS_CHILD.toString(new String[]{"xpath=" + this.simplify().getText(), "nodename=" + ((SchemaNode)current).getIdentifier().getQualifiedName(), "keyword=" + ((SchemaNode)current).getYangKeyword().getLocalName()}));
               }

               SchemaNodeContainer parent = (SchemaNodeContainer)current;
               SchemaNode child = getXPathSchemaChild(parent, childQName);
               if (child == null) {
                  throw new ModelException(Severity.ERROR, xPathContext.getDefineNode(), ErrorCode.INVALID_XPATH_UNRECOGNIZED_CHILD.toString(new String[]{"xpath=" + this.simplify().getText(), "nodename=" + (!parent.isSchemaTreeRoot() ? ((SchemaNode)current).getIdentifier().getQualifiedName() : "/"), "child=" + childQName.getQualifiedName()}));
               }

               if (xPathContext.getDefineNode().isActive() && !child.isActive()) {
                  throw new ModelException(Severity.WARNING, xPathContext.getDefineNode(), ErrorCode.INVALID_XPATH_INACTIVE_CHILD.toString(new String[]{"xpath=" + this.simplify().getText(), "nodename=" + (!parent.isSchemaTreeRoot() ? ((SchemaNode)current).getIdentifier().getQualifiedName() : "/"), "child=" + childQName.getQualifiedName()}));
               }

               current = child;
            }
         }

         return (DataNode)current;
      }
   }

   public Object evaluate(Context context) throws JaxenException {
      List nodeSet = context.getNodeSet();
      List contextNodeSet = new ArrayList(nodeSet);
      YangContextSupport support = (YangContextSupport)context.getContextSupport();
      Context stepContext = new Context(support);
      Iterator stepIter = this.getSteps().iterator();

      while(stepIter.hasNext()) {
         Step eachStep = (Step)stepIter.next();
         stepContext.setNodeSet((List)contextNodeSet);
         contextNodeSet = eachStep.evaluate(stepContext);
         if (support.getEvaluateType() == YangContextSupport.EvaluateType.NEST) {
            int size = ((List)contextNodeSet).size();
            List<YangData> dummyNodes = new ArrayList();

            for(int i = 0; i < size; ++i) {
               Object node = ((List)contextNodeSet).get(i);
               if (node instanceof YangData) {
                  YangData<?> yangData = (YangData)node;
                  boolean result = yangData.checkWhen();
                  if (!result) {
                     if (!yangData.isDummyNode()) {
                        String errorMsg = "the node should not exist,because when condition's validation result is false. condition:" + ((DataDefinition)yangData.getSchemaNode()).getWhen().getArgStr();
                        throw new JaxenException(errorMsg);
                     }

                     dummyNodes.add(yangData);
                  }
               }
            }

            Iterator var15 = dummyNodes.iterator();

            while(var15.hasNext()) {
               YangData dummyNode = (YangData)var15.next();
               ((List)contextNodeSet).remove(dummyNode);
            }
         }

         if (this.isReverseAxis(eachStep)) {
            Collections.reverse((List)contextNodeSet);
         }
      }

//      if (this.getSteps().size() > 1 || nodeSet.size() > 1) {
//         Collections.sort((List)contextNodeSet, new YangNodeComparator(support.getNavigator()));
//      }

      return contextNodeSet;
   }

   public String toString() {
      StringBuffer buf = new StringBuffer();
      Iterator stepIter = this.getSteps().iterator();

      while(stepIter.hasNext()) {
         buf.append(stepIter.next().toString());
         if (stepIter.hasNext()) {
            buf.append("/");
         }
      }

      return buf.toString();
   }

   private boolean isReverseAxis(Step step) {
      int axis = step.getAxis();
      return axis == 8 || axis == 6 || axis == 4 || axis == 13;
   }

   ValidatorResult checkWithSchema(SchemaNode context) {
      return null;
   }
}
