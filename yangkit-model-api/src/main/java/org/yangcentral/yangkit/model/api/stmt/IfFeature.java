package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;

public interface IfFeature extends YangBuiltinStatement {
   boolean evaluate();

   IfFeatureExpr getIfFeatureExpr();

   public interface IfFeatureExpr {
      boolean evaluate();

      ValidatorResult validate();
   }
}
