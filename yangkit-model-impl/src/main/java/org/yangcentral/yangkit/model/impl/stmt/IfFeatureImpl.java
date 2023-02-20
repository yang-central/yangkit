package org.yangcentral.yangkit.model.impl.stmt;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.yangcentral.yangkit.antlr.IfFeatureExpressionBaseVisitor;
import org.yangcentral.yangkit.antlr.IfFeatureExpressionLexer;
import org.yangcentral.yangkit.antlr.IfFeatureExpressionParser;
import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.*;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IfFeatureImpl extends YangSimpleStatementImpl implements IfFeature {
   private IfFeature.IfFeatureExpr ifFeatureExpr;

   public IfFeatureImpl(String argStr) {
      super(argStr);
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case GRAMMAR:
            validatorResultBuilder.merge(this.ifFeatureExpr.validate());
         default:
            return validatorResultBuilder.build();
      }
   }

   public boolean evaluate() {
      return this.ifFeatureExpr.evaluate();
   }

   public IfFeature.IfFeatureExpr getIfFeatureExpr() {
      return this.ifFeatureExpr;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.IFFEATURE.getQName();
   }

   @Override
   protected void clearSelf() {
      this.ifFeatureExpr = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      try {
         if (this.getContext().getCurModule().getEffectiveYangVersion().equals("1") && !ModelUtil.isIdentifierRef(this.getArgStr())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INVALID_ARG.getFieldName()));
            return validatorResultBuilder.build();
         } else {
            IfFeatureExpressionParser parser = new IfFeatureExpressionParser(new CommonTokenStream(new IfFeatureExpressionLexer(CharStreams.fromString(this.getArgStr()))));
            IfFeatureExpressionVisitor visitor = new IfFeatureExpressionVisitor();
            IfFeature.IfFeatureExpr ifFeatureExpr = visitor.visit(parser.if_feature_expr());
            this.ifFeatureExpr = ifFeatureExpr;
            return validatorResultBuilder.build();
         }
      } catch (RuntimeException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ARG.getFieldName()));
         return validatorResultBuilder.build();
      }
   }

   class IfFeatureExpressionVisitor extends IfFeatureExpressionBaseVisitor<IfFeature.IfFeatureExpr> {
      public IfFeature.IfFeatureExpr visitIf_feature_expr(IfFeatureExpressionParser.If_feature_exprContext ctx) {
         Expression expression = IfFeatureImpl.this.new Expression();
         IfFeatureExpressionParser.If_feature_termContext termContext = ctx.if_feature_term();
         expression.ifFeatureTerm = (Term)this.visitIf_feature_term(termContext);
         if (ctx.if_feature_expr() != null) {
            expression.another = (Expression)this.visitIf_feature_expr(ctx.if_feature_expr());
         }

         return expression;
      }

      public IfFeature.IfFeatureExpr visitIf_feature_term(IfFeatureExpressionParser.If_feature_termContext ctx) {
         Term term = IfFeatureImpl.this.new Term();
         term.factor = (Factor)this.visitIf_feature_factor(ctx.if_feature_factor());
         if (ctx.if_feature_term() != null) {
            term.another = (Term)this.visitIf_feature_term(ctx.if_feature_term());
         }

         return term;
      }

      public IfFeature.IfFeatureExpr visitIf_feature_factor(IfFeatureExpressionParser.If_feature_factorContext ctx) {
         if (ctx.if_feature_factor() != null) {
            NotFactor notFactor = IfFeatureImpl.this.new NotFactor();
            notFactor.factor = (Factor)this.visitIf_feature_factor(ctx.if_feature_factor());
            return notFactor;
         } else if (ctx.if_feature_expr() != null) {
            GroupFactor groupFactor = IfFeatureImpl.this.new GroupFactor();
            groupFactor.expression = (Expression)this.visitIf_feature_expr(ctx.if_feature_expr());
            return groupFactor;
         } else if (ctx.identifier_ref_arg() != null) {
            RefFactor refFactor = IfFeatureImpl.this.new RefFactor();
            refFactor.refFeature = new FName(ctx.identifier_ref_arg().getText());
            return refFactor;
         } else {
            return null;
         }
      }

      public IfFeature.IfFeatureExpr visitIdentifier_ref_arg(IfFeatureExpressionParser.Identifier_ref_argContext ctx) {
         return super.visitIdentifier_ref_arg(ctx);
      }
   }

   public class RefFactor extends Factor {
      FName refFeature;
      Feature feature;

      public RefFactor() {
         super();
      }

      public FName getRefFeature() {
         return this.refFeature;
      }

      public Feature getFeature() {
         return this.feature;
      }

      public boolean evaluate() {
         YangSchemaContext schemaContext = IfFeatureImpl.this.getContext().getSchemaContext();
         YangSchema yangSchema = schemaContext.getYangSchema();
         if (yangSchema == null) {
            return true;
         } else {
            Module curModule = this.feature.getContext().getCurModule();
            List<ModuleSet> moduleSets = yangSchema.getModuleSets();

            for (ModuleSet moduleSet : moduleSets) {
               boolean matchedModule = false;

               for (YangModuleDescription moduleDescription : moduleSet.getModules()) {
                  if (curModule instanceof MainModule) {
                     if (moduleDescription.getModuleId().equals(new ModuleId(curModule.getArgStr(), curModule.getCurRevisionDate().isPresent() ? curModule.getCurRevisionDate().get() : null))) {
                        matchedModule = true;
                     }
                  } else {

                     for (ModuleId subModuleDescription : moduleDescription.getSubModules()) {
                        if (subModuleDescription.equals(new ModuleId(curModule.getArgStr(), curModule.getCurRevisionDate().isPresent() ? curModule.getCurRevisionDate().get() : null))) {
                           matchedModule = true;
                           break;
                        }
                     }
                  }

                  if (matchedModule) {
                     Iterator<String> iterator = moduleDescription.getFeatures().iterator();

                     String featureStr;
                     do {
                        if (!iterator.hasNext()) {
                           return false;
                        }

                        featureStr = iterator.next();
                     } while (!this.feature.getArgStr().equals(featureStr));

                     return true;
                  }
               }
            }

            return false;
         }
      }

      public ValidatorResult validate() {
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         this.feature =null;
         try {
            Import im = IfFeatureImpl.this.getContext().getCurModule().getImportByPrefix(this.refFeature.getPrefix());
            if (im != null) {
               if(!im.isReferencedBy(IfFeatureImpl.this)){
                  im.addReference(IfFeatureImpl.this);
               }
            }

            Module module = ModelUtil.findModuleByPrefix(IfFeatureImpl.this.getContext(), this.refFeature.getPrefix());
            Feature feature = module.getFeature(this.refFeature.getLocalName());
            if (feature == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(IfFeatureImpl.this,
                       ErrorCode.UNRECOGNIZED_FEATURE.toString(new String[]{"name=" + this.refFeature})));
               return validatorResultBuilder.build();
            }

            this.feature = feature;
         } catch (ModelException e) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),e.getSeverity(),
                    ErrorTag.BAD_ELEMENT,e.getDescription()));
         }

         return validatorResultBuilder.build();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof RefFactor)) {
            return false;
         } else {
            RefFactor refFactor = (RefFactor)o;
            return this.getFeature().equals(refFactor.getFeature());
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.getFeature()});
      }
   }

   public class GroupFactor extends Factor {
      Expression expression;

      public GroupFactor() {
         super();
      }

      public Expression getExpression() {
         return this.expression;
      }

      public boolean evaluate() {
         return this.expression.evaluate();
      }

      public ValidatorResult validate() {
         return (new ValidatorResultBuilder(this.expression.validate())).build();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof GroupFactor)) {
            return false;
         } else {
            GroupFactor that = (GroupFactor)o;
            return this.getExpression().equals(that.getExpression());
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.getExpression()});
      }
   }

   public class NotFactor extends Factor {
      Factor factor;

      public NotFactor() {
         super();
      }

      public Factor getFactor() {
         return this.factor;
      }

      public boolean evaluate() {
         return !this.factor.evaluate();
      }

      public ValidatorResult validate() {
         return (new ValidatorResultBuilder(this.factor.validate())).build();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof NotFactor)) {
            return false;
         } else {
            NotFactor notFactor = (NotFactor)o;
            return this.getFactor().equals(notFactor.getFactor());
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.getFactor()});
      }
   }

   public abstract class Factor implements IfFeature.IfFeatureExpr {
      IfFeature self;

      @Override
      public IfFeature getSelf() {
         return self;
      }
   }

   public class Term implements IfFeature.IfFeatureExpr {
      Factor factor;
      Term another;

      IfFeature self;

      @Override
      public IfFeature getSelf() {
         return self;
      }

      public Factor getFactor() {
         return this.factor;
      }

      public Term getAnother() {
         return this.another;
      }

      public boolean evaluate() {
         if (this.another == null) {
            return this.factor.evaluate();
         } else {
            return this.factor.evaluate() && this.another.evaluate();
         }
      }

      public ValidatorResult validate() {
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(this.factor.validate());
         if (this.another != null) {
            validatorResultBuilder.merge(this.another.validate());
         }

         return validatorResultBuilder.build();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof Term)) {
            return false;
         } else {
            Term term = (Term)o;
            return Objects.equals(this.getFactor(), term.getFactor()) && Objects.equals(this.getAnother(), term.getAnother());
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.getFactor(), this.getAnother()});
      }
   }

   public class Expression implements IfFeature.IfFeatureExpr {
      Term ifFeatureTerm;
      Expression another;
      IfFeature self;

      @Override
      public IfFeature getSelf() {
         return self;
      }

      public Term getIfFeatureTerm() {
         return this.ifFeatureTerm;
      }

      public Expression getAnother() {
         return this.another;
      }

      public boolean evaluate() {
         if (this.another == null) {
            return this.ifFeatureTerm.evaluate();
         } else {
            return this.ifFeatureTerm.evaluate() || this.another.evaluate();
         }
      }

      public ValidatorResult validate() {
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         validatorResultBuilder.merge(this.ifFeatureTerm.validate());
         if (null != this.another) {
            validatorResultBuilder.merge(this.another.validate());
         }

         return validatorResultBuilder.build();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof Expression)) {
            return false;
         } else {
            Expression that = (Expression)o;
            return Objects.equals(this.getIfFeatureTerm(), that.getIfFeatureTerm()) && Objects.equals(this.getAnother(), that.getAnother());
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.getIfFeatureTerm(), this.getAnother()});
      }
   }
}
