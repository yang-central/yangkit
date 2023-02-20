package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Status;
import org.yangcentral.yangkit.model.api.stmt.StatusStmt;
import org.yangcentral.yangkit.util.ModelUtil;

public class StatusImpl extends YangSimpleStatementImpl implements StatusStmt {
   private Status status;

   public StatusImpl(String argStr) {
      super(argStr);
      this.status = Status.CURRENT;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.STATUS.getQName();
   }

   public Status getStatus() {
      return this.status;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      Status status = Status.getStatus(this.getArgStr());
      if (status == null) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         this.status = status;
         ValidatorResult result = validatorResultBuilder.build();
         return result;
      }
   }
}
