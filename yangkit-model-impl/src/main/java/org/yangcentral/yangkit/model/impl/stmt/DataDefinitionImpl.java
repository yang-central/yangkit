package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilderFactory;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.When;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import org.yangcentral.yangkit.xpath.impl.YangXPathContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathValidator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DataDefinitionImpl extends SchemaNodeImpl implements DataDefinition {
   private When when;
   private boolean whenValidating;
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();

   public DataDefinitionImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.ifFeatureSupport.setYangContext(context);
   }

   public List<IfFeature> getIfFeatures() {
      return this.ifFeatureSupport.getIfFeatures();
   }

   public ValidatorResult addIfFeature(IfFeature ifFeature) {
      return this.ifFeatureSupport.addIfFeature(ifFeature);
   }

   @Override
   public IfFeature getIfFeature(String exp) {
      return ifFeatureSupport.getIfFeature(exp);
   }

   @Override
   public IfFeature removeIfFeature(String exp) {
      return ifFeatureSupport.removeIfFeature(exp);
   }

   public void setIfFeatures(List<IfFeature> ifFeatures) {
      this.ifFeatureSupport.setIfFeatures(ifFeatures);
   }

   public boolean evaluateFeatures() {
      return this.ifFeatureSupport.evaluateFeatures();
   }

   public When getWhen() {
      return this.when;
   }

   public void setWhen(When when) {
      this.when = when;
   }

   public ValidatorResult validateWhen() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.when == null) {
         return validatorResultBuilder.build();
      } else if (this.whenValidating) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         this.whenValidating = true;
         YangXPath xpath = this.when.getXPathExpression();
         Object contextNode = XPathUtil.getXPathContextNode(this);
         YangXPathContext yangXPathContext = new YangXPathContext(this.when.getContext(), contextNode, this);
         xpath.setXPathContext(yangXPathContext);
         YangXPathValidator yangXPathValidator = new YangXPathValidator(xpath, yangXPathContext, new ValidatorResultBuilderFactory(), 1);
         validatorResultBuilder.merge(yangXPathValidator.visit(xpath.getRootExpr(), contextNode));
         this.whenValidating = false;
         return validatorResultBuilder.build();
      }
   }

   @Override
   public boolean checkChild(YangStatement subStatement) {
      boolean result = super.checkChild(subStatement);
      if(!result){
         return false;
      }
      YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
      switch (builtinKeyword){
         case IFFEATURE:{
            if(this.getSubStatement(builtinKeyword.getQName(),subStatement.getArgStr()) != null){
               return false;
            }
            return true;
         }
         default:{
            return true;
         }
      }
   }

   @Override
   protected void clear() {
      this.when = null;
      this.ifFeatureSupport.removeIfFeatures();
      super.clear();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.WHEN.getQName());
      if (matched.size() != 0) {
         When when = (When)matched.get(0);
         this.setWhen(when);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());
      Iterator statementIterator = matched.iterator();

      while(statementIterator.hasNext()) {
         YangStatement statement = (YangStatement)statementIterator.next();
         IfFeature ifFeature = (IfFeature)statement;
         validatorResultBuilder.merge(this.ifFeatureSupport.addIfFeature(ifFeature));
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      validatorResultBuilder.merge(this.validateWhen());
      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.when != null) {
         statements.add(this.when);
      }

      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
