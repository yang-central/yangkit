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
import org.yangcentral.yangkit.model.api.stmt.OrderBy;
import org.yangcentral.yangkit.model.api.stmt.OrderedBy;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

public class OrderedByImpl extends YangBuiltInStatementImpl implements OrderedBy {
   private OrderBy orderBy;

   public OrderedByImpl(String argStr) {
      super(argStr);
   }

   public OrderBy getOrderedBy() {
      return this.orderBy;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ORDEREDBY.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.orderBy = OrderBy.getOrderBy(this.getArgStr());
      if (this.orderBy == null) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      return validatorResultBuilder.build();
   }
}
