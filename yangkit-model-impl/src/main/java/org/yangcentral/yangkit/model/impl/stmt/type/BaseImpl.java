package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Base;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public class BaseImpl extends YangBuiltInStatementImpl implements Base {
   private Identity identity;

   public BaseImpl(String argStr) {
      super(argStr);
   }

   public Identity getIdentity() {
      return this.identity;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.BASE.getQName();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case GRAMMAR:
            FName fName = new FName(this.getArgStr());
            Import im = this.getContext().getCurModule().getImportByPrefix(fName.getPrefix());
            if (im != null) {
               im.addReference(this);
            }

            ValidatorRecordBuilder validatorRecordBuilder;
            try {
               Module module = ModelUtil.findModuleByPrefix(this.getContext(), fName.getPrefix());
               this.identity = module.getIdentity(fName.getLocalName());
               if (this.identity == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_IDENTITY.toString(new String[]{"name=" + fName.getLocalName()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            } catch (ModelException e) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(e.getElement());
               validatorRecordBuilder.setErrorPath(e.getElement().getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(e.getDescription()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public YangStatement getReferenceStatement() {
      return this.identity;
   }
}
