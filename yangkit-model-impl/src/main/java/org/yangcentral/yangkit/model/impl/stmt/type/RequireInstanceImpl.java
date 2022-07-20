package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public class RequireInstanceImpl extends YangBuiltInStatementImpl implements RequireInstance {
   private boolean value;

   public RequireInstanceImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.REQUIREINSTANCE.getQName();
   }

   public boolean value() {
      return this.value;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (!this.getArgStr().equals("true") && !this.getArgStr().equals("false")) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ARG.getFieldName()));
      }

      this.value = Boolean.getBoolean(this.getArgStr());
      ValidatorResult result = validatorResultBuilder.build();
      return result;
   }
}
