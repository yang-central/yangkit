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
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

public class MandatoryImpl extends YangSimpleStatementImpl implements Mandatory {
   public MandatoryImpl(String argStr) {
      super(argStr);
   }

   public boolean getValue() {
      return Boolean.getBoolean(this.getArgStr());
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MANDATORY.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (!this.getArgStr().equals("true") && !this.getArgStr().equals("false")) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName()));
      }

      ValidatorResult result = validatorResultBuilder.build();
      return result;
   }
}
