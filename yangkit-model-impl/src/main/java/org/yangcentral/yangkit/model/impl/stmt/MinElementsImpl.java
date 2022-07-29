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
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

public class MinElementsImpl extends YangSimpleStatementImpl implements MinElements {
   private Integer value;

   public MinElementsImpl(String argStr) {
      super(argStr);
   }

   public Integer getValue() {
      return this.value;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MINELEMENTS.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.value = Integer.valueOf(this.getArgStr());
      if (this.value < 0) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
