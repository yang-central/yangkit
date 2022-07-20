package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
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
               if(!im.isReferencedBy(this)){
                  im.addReference(this);
               }
            }
            try {
               Module module = ModelUtil.findModuleByPrefix(this.getContext(), fName.getPrefix());
               this.identity = module.getIdentity(fName.getLocalName());
               if (this.identity == null) {
                   validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.UNRECOGNIZED_IDENTITY.toString(new String[]{"name=" + fName.getLocalName()})));
               }
            } catch (ModelException e) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),e.getDescription()));
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public YangStatement getReferenceStatement() {
      return this.identity;
   }
}
