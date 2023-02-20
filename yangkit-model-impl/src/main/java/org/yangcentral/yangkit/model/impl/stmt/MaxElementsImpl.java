package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.util.ModelUtil;

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
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.INVALID_ARG.getFieldName()));
            }
         } catch (RuntimeException e) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INVALID_ARG.getFieldName()));
         }
      }

      return validatorResultBuilder.build();
   }
}
