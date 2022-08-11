package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Bits;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.Bit;
import org.yangcentral.yangkit.model.api.stmt.type.Position;
import org.yangcentral.yangkit.model.impl.stmt.EntityImpl;
import org.yangcentral.yangkit.model.impl.stmt.IfFeatureSupportImpl;
import org.yangcentral.yangkit.register.YangStatementRegister;

import java.util.ArrayList;
import java.util.List;

public class BitImpl extends EntityImpl implements Bit {
   private Position position;
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();

   public BitImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.BIT.getQName();
   }

   public Position getPosition() {
      return this.position;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.POSITION.getQName());
      if (matched.size() != 0) {
         this.position = (Position)matched.get(0);
      }
      matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());
      if(matched.size() >0){
         for(YangStatement subStatement:matched){
            validatorResultBuilder.merge(this.addIfFeature((IfFeature) subStatement));
         }
      }

      return validatorResultBuilder.build();
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

   @Override
   protected void clear() {
      position = null;
      ifFeatureSupport.removeIfFeatures();
      super.clear();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.position != null) {
         statements.add(this.position);
      } else {
         Type type = (Type)this.getParentStatement();
         Bits bits = (Bits)type.getRestriction();
         Long actualVal = bits.getBitActualPosition(this.getArgStr());
         Position newPos = (Position) YangStatementRegister.getInstance().getYangStatementInstance(YangBuiltinKeyword.POSITION.getQName(),actualVal.toString());
         if(newPos != null){
            newPos.setContext(this.getContext());
            newPos.setElementPosition(this.getElementPosition());
            statements.add(newPos);
         }
      }

      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
