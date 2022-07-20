package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.type.Position;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public class PositionImpl extends YangBuiltInStatementImpl implements Position {
   private long value;

   public PositionImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.POSITION.getQName();
   }

   public long getValue() {
      return this.value;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      try {
         this.value = Long.parseLong(this.getArgStr());
         if (this.value < 0L) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INVALID_BIT_POSTION.getFieldName()));
         }
      } catch (RuntimeException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_BIT_POSTION.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
