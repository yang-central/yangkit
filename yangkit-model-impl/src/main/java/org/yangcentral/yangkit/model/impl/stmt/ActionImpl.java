package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActionImpl extends OperationImpl implements Action {

   public ActionImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ACTION.getQName();
   }

   private boolean isAncestorNodeNoKey() {
      for (SchemaNodeContainer parent = this.getParentSchemaNode(); parent != null; parent = ((SchemaNode) parent).getParentSchemaNode()) {
         if (!(parent instanceof SchemaNode)) {
            return false;
         }

         if (parent instanceof YangList) {
            YangList list = (YangList) parent;
            if (list.getKey() == null) {
               return true;
            }
         }
      }

      return false;
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      if (this.isAncestorNodeNoKey()) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this, ErrorCode.ACTION_IN_LIST_NO_KEY.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}