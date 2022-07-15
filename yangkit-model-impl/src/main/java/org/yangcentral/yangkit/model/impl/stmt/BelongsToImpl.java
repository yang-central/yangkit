package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
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
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.BelongsTo;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Prefix;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

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
      assert phase == BuildPhase.LINKAGE;

      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      String moduleName = this.getArgStr();
      List<Module> modules = this.getContext().getSchemaContext().getModule(moduleName);
      if (modules != null) {
         Iterator moduleIterator = modules.iterator();

         while(moduleIterator.hasNext()) {
            Module module = (Module)moduleIterator.next();
            if (module instanceof SubModule) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.WRONG_TYPE_DEPENDECE_MODULE.getFieldName() + " It MUST be module, but get a submodule."));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               this.mainModules.add((MainModule)module);
            }
         }
      }

      if (this.mainModules.size() == 0) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + moduleName})));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.PREFIX.getQName());
      if (matched.size() != 0) {
         this.prefix = (Prefix)matched.get(0);
         Module curModule = this.getContext().getCurModule();
         Map<String, ModuleId> prefixes = curModule.getPrefixes();
         if (prefixes.containsKey(this.prefix.getArgStr())) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this.prefix);
            validatorRecordBuilder.setErrorPath(this.prefix.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
