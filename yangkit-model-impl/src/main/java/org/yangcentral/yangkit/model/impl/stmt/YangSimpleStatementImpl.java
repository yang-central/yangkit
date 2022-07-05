package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;

abstract class YangSimpleStatementImpl extends YangBuiltInStatementImpl {
   public YangSimpleStatementImpl(String argStr) {
      super(argStr);
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      return (new ValidatorResultBuilder()).build();
   }
}
