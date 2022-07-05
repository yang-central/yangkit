package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

public class MaxElementsImpl extends YangBuiltInStatementImpl implements MaxElements {
   private boolean unbounded = true;
   private Integer value;

   public MaxElementsImpl(String argStr) {
      super(argStr);
   }

   public boolean isUnbounded() {
      return this.unbounded;
   }

   public Integer getValue() {
      return this.value;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MAXELEMENTS.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (this.getArgStr().equals("unbounded")) {
         this.unbounded = true;
      } else {
         this.unbounded = false;

         try {
            this.value = Integer.valueOf(this.getArgStr());
            if (this.value <= 0) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         } catch (RuntimeException var4) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }
      }

      return validatorResultBuilder.build();
   }
}
