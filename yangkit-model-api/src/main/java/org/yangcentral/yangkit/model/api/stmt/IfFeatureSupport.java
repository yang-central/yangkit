package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface IfFeatureSupport {
   List<IfFeature> getIfFeatures();
   ValidatorResult addIfFeature(IfFeature ifFeature);

   void setIfFeatures(List<IfFeature> ifFeatures);

   boolean evaluateFeatures();
}
