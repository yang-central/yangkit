package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Feature;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureImpl extends EntityImpl implements Feature {
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();

   public FeatureImpl(String argStr) {
      super(argStr);
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

   public QName getYangKeyword() {
      return YangBuiltinKeyword.FEATURE.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.ifFeatureSupport.removeIfFeatures();
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());
      Iterator statementIterator = matched.iterator();

      while(statementIterator.hasNext()) {
         YangStatement statement = (YangStatement)statementIterator.next();
         IfFeature ifFeature = (IfFeature)statement;
         validatorResultBuilder.merge(this.addIfFeature(ifFeature));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
