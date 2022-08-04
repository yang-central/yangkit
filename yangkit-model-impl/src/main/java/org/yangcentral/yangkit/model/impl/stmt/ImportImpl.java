package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Prefix;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.RevisionDate;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportImpl extends YangStatementImpl implements Import {
   private Prefix prefix;
   private RevisionDate revisionDate;
   private Description description;
   private Reference reference;
   private MainModule importedModule;
   private List<YangStatement> referencedStmts = new ArrayList<>();

   public ImportImpl(String argStr) {
      super(argStr);
   }

   public Prefix getPrefix() {
      return this.prefix;
   }

   public RevisionDate getRevisionDate() {
      return this.revisionDate;
   }

   public Optional<MainModule> getImport() {
      return null == this.importedModule ? Optional.empty() : Optional.of(this.importedModule);
   }


   @Override
   public List<YangStatement> getReferencedBy() {
      return referencedStmts;
   }


   public Description getDescription() {
      return this.description;
   }

   public void setDescription(Description description) {
      this.description = description;
   }

   public Reference getReference() {
      return this.reference;
   }

   public void setReference(Reference reference) {
      this.reference = reference;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.IMPORT.getQName();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      assert phase == BuildPhase.LINKAGE;

      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      YangContext context = this.getContext();
      YangSchemaContext schemaContext = context.getSchemaContext();
      boolean notFound = false;
      boolean wrongType = false;
      this.importedModule = null;
      if (this.revisionDate == null) {
         List<org.yangcentral.yangkit.model.api.stmt.Module> moduleList = schemaContext.getModule(this.getArgStr());
         if (null != moduleList && moduleList.size() != 0) {
            if (moduleList.size() > 1) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.TOO_MANY_DEPENDECE_MODULES.getFieldName()));
               return validatorResultBuilder.build();
            }

            if (!(moduleList.get(0) instanceof MainModule)) {
               wrongType = true;
            } else {
               this.importedModule = (MainModule)moduleList.get(0);
            }
         } else {
            notFound = true;
         }
      } else {
         Optional<Module> moduleOp = schemaContext.getModule(this.getArgStr(), this.revisionDate.getArgStr());
         if (!moduleOp.isPresent()) {
            notFound = true;
         } else if (!(moduleOp.get() instanceof MainModule)) {
            wrongType = true;
         } else {
            this.importedModule = (MainModule)moduleOp.get();
         }
      }

      if (notFound) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + this.getArgStr()})));
         return validatorResultBuilder.build();
      } else if (wrongType) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.WRONG_TYPE_DEPENDECE_MODULE.getFieldName() + " It should be a module, but get a submodule."));
         return validatorResultBuilder.build();
      } else {
         if (this.importedModule.getEffectiveYangVersion().equals("1.1") && this.getContext().getCurModule().getEffectiveYangVersion().equals("1")) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,Severity.WARNING,
                    ErrorTag.BAD_ELEMENT,ErrorCode.INCOMPATIBLE_YANG_VERSION.getFieldName()));
         }

         ValidatorResult importModuleResult;
         importModuleResult = this.importedModule.init();
         if (!importModuleResult.isOk()) {
            validatorResultBuilder.merge(importModuleResult);
            return validatorResultBuilder.build();
         }

         importModuleResult = this.importedModule.build();
         if (!importModuleResult.isOk()) {
            if (schemaContext.isImportOnly(this.importedModule)) {
               validatorResultBuilder.merge(importModuleResult);
            } else {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.IMPORT_MODULE_NOT_VALIDATE.toString(new String[]{"module=" + this.getArgStr()})));
            }
         }

         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.description = null;
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() != 0) {
         this.description = (Description)matched.get(0);
      }

      this.reference = null;
      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() != 0) {
         this.reference = (Reference)matched.get(0);
      }
      this.revisionDate = null;
      matched = this.getSubStatement(YangBuiltinKeyword.REVISIONDATE.getQName());
      if (matched.size() != 0) {
         this.revisionDate = (RevisionDate)matched.get(0);
      }
      Module curModule = this.getContext().getCurModule();
      Map<String, ModuleId> prefixes = curModule.getPrefixes();
      if(this.prefix != null){
         prefixes.remove(this.prefix.getArgStr());
      }
      this.prefix = null;
      matched = this.getSubStatement(YangBuiltinKeyword.PREFIX.getQName());
      if (matched.size() != 0) {
         this.prefix = (Prefix)matched.get(0);
      }


      if (prefixes.containsKey(this.prefix.getArgStr())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.prefix,
                 ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         prefixes.put(this.prefix.getArgStr(), new ModuleId(this.getArgStr(),
                 this.revisionDate == null ? null : this.revisionDate.getArgStr()));
         return validatorResultBuilder.build();
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.prefix != null) {
         statements.add(this.prefix);
      }

      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      if (this.revisionDate != null) {
         statements.add(this.revisionDate);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }

}
