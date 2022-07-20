package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IfFeatureSupportImpl implements IfFeatureSupport {
   private List<IfFeature> ifFeatures = new ArrayList();
   private YangContext yangContext;

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public List<IfFeature> getIfFeatures() {
      return Collections.unmodifiableList(this.ifFeatures);
   }


   public void removeIfFeatures() {
      ifFeatures.clear();
   }


   public IfFeature removeIfFeature(String arg) {
      for(IfFeature ifFeature:ifFeatures){
         if(ifFeature.getArgStr().equals(arg)){
            ifFeatures.remove(ifFeature);
            return ifFeature;
         }
      }
      return null;
   }

   public ValidatorResult addIfFeature(IfFeature ifFeature) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator iterator = this.ifFeatures.iterator();

      IfFeature feature;
      do {
         if (!iterator.hasNext()) {
            this.ifFeatures.add(ifFeature);
            return validatorResultBuilder.build();
         }

         feature = (IfFeature)iterator.next();
      } while(!feature.getArgStr().equals(ifFeature.getArgStr()));

      validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(feature, ifFeature));
      ifFeature.setErrorStatement(true);
      return validatorResultBuilder.build();
   }

   public void setIfFeatures(List<IfFeature> ifFeatures) {
      this.ifFeatures = ifFeatures;
   }

   public boolean evaluateFeatures() {
      Iterator var1 = this.ifFeatures.iterator();

      IfFeature ifFeature;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         ifFeature = (IfFeature)var1.next();
      } while(ifFeature.evaluate());

      return false;
   }
}
