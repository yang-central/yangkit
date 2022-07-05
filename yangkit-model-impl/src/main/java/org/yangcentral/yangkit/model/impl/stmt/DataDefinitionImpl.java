package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilderFactory;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.When;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import org.yangcentral.yangkit.xpath.YangXPath;
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
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         this.whenValidating = true;
         YangXPath xpath = this.when.getXPathExpression();
         Object contextNode = XPathUtil.getXPathContextNode(this);
         YangXPathContext yangXPathContext = new YangXPathContext(this.when.getContext(), contextNode, this);
         xpath.setXPathContext(yangXPathContext);
         YangXPathValidator yangXPathValidator = new YangXPathValidator(xpath, yangXPathContext, new ValidatorResultBuilderFactory(), 1);
         validatorResultBuilder.merge((ValidatorResult)yangXPathValidator.visit(xpath.getRootExpr(), contextNode));
         this.whenValidating = false;
         return validatorResultBuilder.build();
      }
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
      Iterator var6 = matched.iterator();

      while(var6.hasNext()) {
         YangStatement statement = (YangStatement)var6.next();
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
