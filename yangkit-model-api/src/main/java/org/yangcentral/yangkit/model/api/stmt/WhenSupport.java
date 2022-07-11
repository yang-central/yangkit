package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;

public interface WhenSupport {
   When getWhen();

   void setWhen(When when);

   ValidatorResult validateWhen();
}
