package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.type.Value;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public class ValueImpl extends YangBuiltInStatementImpl implements Value {
   private int value;

   public ValueImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.VALUE.getQName();
   }

   public int getValue() {
      return this.value;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      try {
         this.value = Integer.parseInt(this.getArgStr());
      } catch (RuntimeException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ENUM_VALUE.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
