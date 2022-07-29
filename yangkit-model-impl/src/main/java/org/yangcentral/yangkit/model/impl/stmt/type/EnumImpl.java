package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Enumeration;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.Value;
import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;
import org.yangcentral.yangkit.model.impl.stmt.EntityImpl;
import org.yangcentral.yangkit.model.impl.stmt.IfFeatureSupportImpl;
import org.yangcentral.yangkit.register.YangStatementRegister;

import java.util.ArrayList;
import java.util.List;

public class EnumImpl extends EntityImpl implements YangEnum {
   private Value value;
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();

   public EnumImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ENUM.getQName();
   }

   public Value getValue() {
      return this.value;
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.ifFeatureSupport.setYangContext(context);
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      this.value = null;
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.VALUE.getQName());
      if (matched.size() != 0) {
         this.value = (Value)matched.get(0);
      }

      return validatorResultBuilder.build();
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

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.value != null) {
         statements.add(this.value);
      } else {
         Type type = (Type)this.getParentStatement();
         Enumeration enumeration = (Enumeration)type.getRestriction();
         Integer actualVal = enumeration.getEnumActualValue(this.getArgStr());
         Value newVal = (Value) YangStatementRegister.getInstance().getYangStatementInstance(YangBuiltinKeyword.VALUE.getQName(),actualVal.toString());
         if(newVal != null){
            newVal.setContext(this.getContext());
            newVal.setElementPosition(this.getElementPosition());
            statements.add(newVal);
         }

      }

      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
