package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.BelongsTo;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class SubModuleImpl extends ModuleImpl implements SubModule {
   private BelongsTo belongsTo;

   public SubModuleImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.SUBMODULE.getQName();
   }

   public BelongsTo getBelongsto() {
      return this.belongsTo;
   }

   @Override
   protected void clearSelf() {
      this.belongsTo = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName());
      if (null != matched && matched.size() > 0) {
         this.belongsTo = (BelongsTo)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public boolean isSelfPrefix(String prefix) {
      return this.getBelongsto().getPrefix().getArgStr().equals(prefix);
   }

   public String getSelfPrefix() {
      return this.getBelongsto().getPrefix().getArgStr();
   }

   public MainModule getMainModule() {
      return this.getBelongsto().getMainModules().size() == 0 ? null : (MainModule)this.getBelongsto().getMainModules().get(0);
   }

   public List<YangStatement> getEffectiveLinkageStatement() {
      List<YangStatement> statements = new ArrayList<>();
      statements.addAll(super.getEffectiveLinkageStatement());
      if (this.belongsTo != null) {
         statements.add(this.belongsTo);
      }

      return statements;
   }
}
