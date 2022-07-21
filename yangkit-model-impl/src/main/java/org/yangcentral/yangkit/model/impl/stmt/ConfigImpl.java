package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Config;
import org.yangcentral.yangkit.util.ModelUtil;

public class ConfigImpl extends YangSimpleStatementImpl implements Config {
   public ConfigImpl(String argStr) {
      super(argStr);
   }

   public boolean isConfig() {
      return Boolean.valueOf(this.getArgStr());
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.CONFIG.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (!this.getArgStr().equals("true") && !this.getArgStr().equals("false")) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ARG.getFieldName()));
      }

      ValidatorResult result = validatorResultBuilder.build();
      return result;
   }
}
