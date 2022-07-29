package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface IfFeatureSupport {
   List<IfFeature> getIfFeatures();
   ValidatorResult addIfFeature(IfFeature ifFeature);

   IfFeature getIfFeature(String exp);
   IfFeature removeIfFeature(String exp);

   void setIfFeatures(List<IfFeature> ifFeatures);

   boolean evaluateFeatures();
}
