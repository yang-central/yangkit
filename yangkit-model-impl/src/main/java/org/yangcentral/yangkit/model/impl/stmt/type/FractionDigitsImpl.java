package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.FractionDigits;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;

import java.util.Objects;

public class FractionDigitsImpl extends YangBuiltInStatementImpl implements FractionDigits {
   private Integer value;

   public FractionDigitsImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.FRACTIONDIGITS.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      this.value = Integer.valueOf(this.getArgStr());
      if (this.value >= 1 && this.value <= 18) {
         return validatorResultBuilder.build();
      } else {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder<>();
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.FRACTIONDIGITS_ERROR.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      }
   }

   public Integer getValue() {
      return this.value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof FractionDigitsImpl)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         FractionDigitsImpl that = (FractionDigitsImpl)o;
         return this.getValue().equals(that.getValue());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.getValue()});
   }
}
