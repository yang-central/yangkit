package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.type.Modifier;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public class ModifierImpl extends YangBuiltInStatementImpl implements Modifier {
   public ModifierImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MODIFIER.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      if (!this.getArgStr().equals("invert-match")) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ARG.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
