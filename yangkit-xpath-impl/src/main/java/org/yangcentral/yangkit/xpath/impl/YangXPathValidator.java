package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.common.api.Builder;
import org.yangcentral.yangkit.common.api.BuilderFactory;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.WhenSupport;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Predicated;
import org.jaxen.expr.Step;
import org.yangcentral.yangkit.xpath.YangXPath;

public class YangXPathValidator extends YangXPathBaseVisitor<ValidatorResult, Object, YangXPathContext> {
   public static final int VALIDATE_TYPE_MUST = 0;
   public static final int VALIDATE_TYPE_WHEN = 1;
   public static final int VALIDATE_TYPE_LEAFREF = 2;
   private int validateType = 0;

   public YangXPathValidator(YangXPath yangXPath, YangXPathContext context, BuilderFactory<ValidatorResult> builderFactory) {
      super(yangXPath, context, builderFactory);
   }

   public YangXPathValidator(YangXPath yangXPath, YangXPathContext context, BuilderFactory<ValidatorResult> builderFactory, int validateType) {
      super(yangXPath, context, builderFactory);
      this.validateType = validateType;
   }

   private ValidatorResult checkPredicateForLeafref(Predicate predicate, Object context) {
      Builder<ValidatorResult> builder = this.getBuilderFactory().getBuilder();
      Expr exprLHS;
      ValidatorRecordBuilder validatorRecordBuilder;
      if (!(context instanceof YangList)) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(((YangXPathContext)this.getContext()).getDefineNode());
         validatorRecordBuilder.setErrorPath(((YangXPathContext)this.getContext()).getDefineNode().getElementPosition());
         exprLHS = null;
         String nodeName;
         if (context instanceof SchemaNodeContainer && ((SchemaNodeContainer)context).isSchemaTreeRoot()) {
            nodeName = "/";
         } else {
            nodeName = ((SchemaNode)context).getIdentifier().getQualifiedName();
         }

         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH_LEAFREF_PREDICATE_ONLY_LIST.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "predicate=" + predicate.getText(), "nodename=" + nodeName})));
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         builder.merge(validatorResultBuilder.build());
         return (ValidatorResult)builder.build();
      } else if (!(predicate.getExpr() instanceof EqualityExpr)) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(((YangXPathContext)this.getContext()).getDefineNode());
         validatorRecordBuilder.setErrorPath(((YangXPathContext)this.getContext()).getDefineNode().getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH_LEAFREF_PREDICATE_MUST_EQUALITY.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText()})));
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         builder.merge(validatorResultBuilder.build());
         return (ValidatorResult)builder.build();
      } else {
         EqualityExpr equalityExpr = (EqualityExpr)predicate.getExpr();
         exprLHS = equalityExpr.getLHS();
         if (!(exprLHS instanceof LocationPath)) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(((YangXPathContext)this.getContext()).getDefineNode());
            validatorRecordBuilder.setErrorPath(((YangXPathContext)this.getContext()).getDefineNode().getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH_LEAFREF_PREDICATE_MUST_KEY.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + exprLHS.getText()})));
            ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            builder.merge(validatorResultBuilder.build());
            return (ValidatorResult)builder.build();
         } else {
            YangLocationPathImpl keyLocation = (YangLocationPathImpl)exprLHS;

            try {
               YangXPathContext keyXPathContext = ((YangXPathContext)this.getContext()).newContext();
               keyXPathContext.setCurrentNode(context);
               SchemaNode keyNode = keyLocation.getTargetSchemaNode(keyXPathContext);
               if (((YangList)context).getKey().getKeyNode(keyNode.getIdentifier()) == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(keyXPathContext.getDefineNode());
                  validatorRecordBuilder.setErrorPath(keyXPathContext.getDefineNode().getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH_LEAFREF_PREDICATE_MUST_KEY.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + ((SchemaNode)context).getIdentifier().getQualifiedName()})));
                  ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  builder.merge(validatorResultBuilder.build());
               }
            } catch (ModelException var11) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(var11.getElement());
               validatorRecordBuilder.setErrorPath(var11.getElement().getElementPosition());
               validatorRecordBuilder.setSeverity(var11.getSeverity());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(var11.getDescription()));
               ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               builder.merge(validatorResultBuilder.build());
               return (ValidatorResult)builder.build();
            }

            return (ValidatorResult)builder.build();
         }
      }
   }

   private Object visitStep(Step step, Object context, Object currentNode) throws ModelException {
      if (step.getAxis() == 3) {
         if (currentNode instanceof SchemaNodeContainer && ((SchemaNodeContainer)currentNode).isSchemaTreeRoot()) {
            throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_XPATH.getFieldName() + " xpath:" + this.getYangXPath().getRootExpr().simplify().getText());
         }

         SchemaNodeContainer parent = YangLocationPathImpl.getXPathSchemaParent((SchemaNode)currentNode);
         currentNode = parent;
      } else if (step.getAxis() == 1) {
         if (step instanceof NameStep) {
            NameStep nameStep = (NameStep)step;
            String prefix = nameStep.getPrefix();
            String localName = nameStep.getLocalName();
            URI namespace = null;
            if (prefix != null && prefix.length() > 0) {
               Module module = ModelUtil.findModuleByPrefix(((YangXPathContext)this.getContext()).getYangContext(), prefix);
               if (null == module) {
                  throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_PREFIX.toString(new String[]{"name=" + prefix}));
               }

               namespace = module.getMainModule().getNamespace().getUri();
            } else {
               Object contextNode = ((YangXPathContext)this.getContext()).getContextNode();
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
            if (!(currentNode instanceof SchemaNodeContainer)) {
               throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_XPATH_TERMIANL_HAS_CHILD.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + ((SchemaNode)currentNode).getIdentifier().getQualifiedName(), "keyword=" + ((SchemaNode)currentNode).getYangKeyword().getLocalName()}));
            }

            SchemaNodeContainer parent = (SchemaNodeContainer)currentNode;
            SchemaNode child = YangLocationPathImpl.getXPathSchemaChild(parent, childQName);
            if (child == null) {
               throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_XPATH_UNRECOGNIZED_CHILD.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + (!((SchemaNodeContainer)currentNode).isSchemaTreeRoot() ? ((SchemaNode)currentNode).getIdentifier().getQualifiedName() : "/"), "child=" + childQName.getQualifiedName()}));
            }

            if (((YangXPathContext)this.getContext()).getDefineNode().isActive() && !child.isActive()) {
               throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_XPATH_INACTIVE_CHILD.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + (!((SchemaNodeContainer)currentNode).isSchemaTreeRoot() ? ((SchemaNode)currentNode).getIdentifier().getQualifiedName() : "/"), "child=" + childQName.getQualifiedName()}));
            }

            if (((YangXPathContext)this.getContext()).getContextNode() instanceof SchemaNode && ((SchemaNode)((YangXPathContext)this.getContext()).getContextNode()).isConfig() && !child.isConfig()) {
               throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), ErrorCode.INVALID_XPATH_UNCMPATIBLE_CHILD.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText(), "nodename=" + (!((SchemaNodeContainer)currentNode).isSchemaTreeRoot() ? ((SchemaNode)currentNode).getIdentifier().getQualifiedName() : "/"), "context=" + ((SchemaNode)((YangXPathContext)this.getContext()).getContextNode()).getIdentifier().getQualifiedName()}));
            }

            if (child instanceof WhenSupport && this.validateType == 1) {
               WhenSupport whenSupport = (WhenSupport)child;
               if (whenSupport.getWhen() != null) {
                  ValidatorResult whenResult = whenSupport.validateWhen();
                  if (!whenResult.isOk()) {
                     throw new ModelException(Severity.WARNING, child, whenResult.toString());
                  }
               }
            }

            currentNode = child;
         }
      } else if (step.getAxis() != 11) {
         throw new IllegalArgumentException("un-support axis.");
      }

      if (step instanceof Predicated) {
         List predicates = step.getPredicates();
         if (predicates.size() > 0) {
            Iterator var15 = predicates.iterator();

            while(var15.hasNext()) {
               Object o1 = var15.next();
               Predicate predicate = (Predicate)o1;
               ValidatorResult predicateResult;
               if (this.validateType == 2) {
                  predicateResult = this.checkPredicateForLeafref(predicate, currentNode);
                  if (!predicateResult.isOk()) {
                     throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), predicateResult.toString());
                  }
               }

               predicateResult = (ValidatorResult)this.visit(predicate.getExpr(), currentNode);
               if (!predicateResult.isOk()) {
                  throw new ModelException(Severity.WARNING, ((YangXPathContext)this.getContext()).getDefineNode(), predicateResult.toString());
               }
            }
         }
      }

      return currentNode;
   }

   public ValidatorResult visitLocationExpr(LocationPath expr, Object context) {
      Builder<ValidatorResult> builder = this.getBuilderFactory().getBuilder();
      Object currentNode = context;
      if (expr.isAbsolute()) {
         if (context instanceof Module) {
            currentNode = new YangXPathRoot((Module)context);
         } else {
            currentNode = new YangXPathRoot((SchemaNode)context);
         }
      }

      List steps = expr.getSteps();
      Iterator var6 = steps.iterator();

      while(var6.hasNext()) {
         Object o = var6.next();
         Step step = (Step)o;

         try {
            currentNode = this.visitStep(step, context, currentNode);
         } catch (ModelException var12) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(var12.getSeverity());
            validatorRecordBuilder.setBadElement(var12.getElement());
            validatorRecordBuilder.setErrorPath(var12.getElement().getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(var12.getDescription()));
            ValidatorResultBuilder stepValidatorResultBuilder = new ValidatorResultBuilder();
            stepValidatorResultBuilder.addRecord(validatorRecordBuilder.build());
            builder.merge(stepValidatorResultBuilder.build());
            return (ValidatorResult)builder.build();
         }
      }

      if (this.validateType == 1 && (currentNode == ((YangXPathContext)this.getContext()).getDefineNode() || ((SchemaNode)currentNode).isAncestorNode(((YangXPathContext)this.getContext()).getDefineNode()))) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(((YangXPathContext)this.getContext()).getDefineNode());
         validatorRecordBuilder.setErrorPath(((YangXPathContext)this.getContext()).getDefineNode().getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH_WHEN_ACCESS_CHILD.toString(new String[]{"xpath=" + this.getYangXPath().getRootExpr().simplify().getText()})));
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         builder.merge(validatorResultBuilder.build());
      }

      return (ValidatorResult)builder.build();
   }

   public ValidatorResult visitPathExpr(PathExpr expr, Object context) {
      Builder<ValidatorResult> builder = this.getBuilderFactory().getBuilder();
      Object locationContext = context;
      Expr filterExpr = expr.getFilterExpr();
      if (filterExpr instanceof FunctionCallExpr) {
         FunctionCallExpr functionCallExpr = (FunctionCallExpr)filterExpr;
         if (!functionCallExpr.getFunctionName().equals("current")) {
            throw new IllegalArgumentException("un-support function.");
         }

         locationContext = ((YangXPathContext)this.getContext()).getContextNode();
      }

      Expr locationExpr = expr.getLocationPath();
      builder.merge((ValidatorResult)this.visit(filterExpr, context));
      builder.merge((ValidatorResult)this.visit(locationExpr, locationContext));
      return (ValidatorResult)builder.build();
   }
}
