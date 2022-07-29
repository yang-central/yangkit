package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangVersion;
import org.yangcentral.yangkit.util.ModelUtil;

public class YangVersionImpl extends YangBuiltInStatementImpl implements YangVersion {
   public YangVersionImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.YANGVERSION.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (!this.getArgStr().equals(Yang.VERSION_1) && !this.getArgStr().equals(Yang.VERSION_11)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_VERSION.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
