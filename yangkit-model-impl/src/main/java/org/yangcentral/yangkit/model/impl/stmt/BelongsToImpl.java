package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BelongsToImpl extends YangBuiltInStatementImpl implements BelongsTo {
   private Prefix prefix;
   private List<MainModule> mainModules = new ArrayList();

   public BelongsToImpl(String argStr) {
      super(argStr);
   }

   public Prefix getPrefix() {
      return this.prefix;
   }

   public List<MainModule> getMainModules() {
      return this.mainModules;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.BELONGSTO.getQName();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {

      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      String moduleName = this.getArgStr();
      this.mainModules.clear();
      List<Module> modules = this.getContext().getSchemaContext().getModule(moduleName);
      if (modules != null) {
         Iterator moduleIterator = modules.iterator();

         while(moduleIterator.hasNext()) {
            Module module = (Module)moduleIterator.next();
            if (module instanceof SubModule) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.WRONG_TYPE_DEPENDECE_MODULE.getFieldName()
                               + " It MUST be module, but get a submodule."));
            } else {
               this.mainModules.add((MainModule)module);
            }
         }
      }

      if (this.mainModules.size() == 0) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + moduleName})));
         return validatorResultBuilder.build();
      } else {
         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.prefix = null;
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.PREFIX.getQName());
      if (matched.size() != 0) {
         this.prefix = (Prefix)matched.get(0);
         Module curModule = this.getContext().getCurModule();
         Map<String, ModuleId> prefixes = curModule.getPrefixes();
         if (prefixes.containsKey(this.prefix.getArgStr())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(prefix,ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            return validatorResultBuilder.build();
         }

         prefixes.put(this.prefix.getArgStr(), new ModuleId(this.getArgStr(), (String)null));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.prefix != null) {
         statements.add(this.prefix);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
