package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Base;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IdentityImpl extends EntityImpl implements Identity {
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();
   private List<Base> bases = new ArrayList<>();

   public IdentityImpl(String argStr) {
      super(argStr);
   }

   public List<Base> getBases() {
      return Collections.unmodifiableList(this.bases);
   }

   public Base getBase(String name) {
      Iterator<Base> baseIterator = this.bases.iterator();

      Base base;
      do {
         if (!baseIterator.hasNext()) {
            return null;
         }

         base = baseIterator.next();
      } while(!base.getArgStr().equals(name));

      return base;
   }

   public boolean isDerived(Identity other) {
      if (this.bases.size() == 0) {
         return false;
      } else {
         Iterator<Base> baseIterator = this.bases.iterator();

         Base base;
         do {
            if (!baseIterator.hasNext()) {
               return false;
            }

            base = baseIterator.next();
         } while(!base.getIdentity().isDerivedOrSelf(other));

         return true;
      }
   }

   public boolean isDerivedOrSelf(Identity other) {
      if (this.equals(other)) {
         return true;
      } else if (this.bases.size() == 0) {
         return false;
      } else {
         Iterator<Base> baseIterator = this.bases.iterator();

         Base base;
         do {
            if (!baseIterator.hasNext()) {
               return false;
            }

            base = baseIterator.next();
         } while(!base.getIdentity().isDerivedOrSelf(other));

         return true;
      }
   }

   public List<IfFeature> getIfFeatures() {
      return this.ifFeatureSupport.getIfFeatures();
   }

   public ValidatorResult addIfFeature(IfFeature ifFeature) {
      return this.ifFeatureSupport.addIfFeature(ifFeature);
   }

   @Override
   public IfFeature getIfFeature(String exp) {
      return ifFeatureSupport.removeIfFeature(exp);
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
      return YangBuiltinKeyword.IDENTITY.getQName();
   }

   @Override
   public boolean checkChild(YangStatement subStatement) {
      boolean result = super.checkChild(subStatement);
      if(!result){
         return false;
      }
      YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
      switch (builtinKeyword){
         case BASE:{
            if(getBase(subStatement.getArgStr()) != null){
               return false;
            }
            return true;
         }
         case IFFEATURE:{
            if(getIfFeature(subStatement.getArgStr()) != null){
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
   protected void clearSelf() {
      this.bases.clear();
      this.ifFeatureSupport.removeIfFeatures();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.BASE.getQName());
      if (matched.size() > 0) {
         for (YangStatement statement : matched) {
            Base base = (Base)statement;
            Base orig = this.getBase(base.getArgStr());
            if (orig != null) {
               validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, base));
               base.setErrorStatement(true);
            } else {
               this.bases.add(base);
            }
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());

      for (YangStatement statement : matched) {
         IfFeature ifFeature = (IfFeature)statement;
         validatorResultBuilder.merge(this.addIfFeature(ifFeature));
      }

      return validatorResultBuilder.build();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Identity)) {
         return false;
      } else {
         IdentityImpl another = (IdentityImpl)obj;
         return this.getArgStr().equals(another.getArgStr());
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(this.bases);
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
