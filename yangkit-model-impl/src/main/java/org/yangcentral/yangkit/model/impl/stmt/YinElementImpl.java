package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.YinElement;
import org.yangcentral.yangkit.util.ModelUtil;

public class YinElementImpl extends YangSimpleStatementImpl implements YinElement {
   private boolean value;

   public YinElementImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.YINELEMENT.getQName();
   }

   public boolean value() {
      return this.value;
   }

   @Override
   protected void clearSelf() {
      this.value = false;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      if (!this.getArgStr().equals("true") && !this.getArgStr().equals("false")) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName()));
      } else {
         this.value = Boolean.getBoolean(this.getArgStr());
      }
      return validatorResultBuilder.build();
   }
}
