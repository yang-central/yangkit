package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Include;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.RevisionDate;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IncludeImpl extends YangStatementImpl implements Include {
   private RevisionDate revisionDate;
   private Description description;
   private Reference reference;
   private SubModule includeModule;

   public IncludeImpl(String argStr) {
      super(argStr);
   }

   public RevisionDate getRevisionDate() {
      return this.revisionDate;
   }

   public Optional<SubModule> getInclude() {
      return null == this.includeModule ? Optional.empty() : Optional.of(this.includeModule);
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
      return YangBuiltinKeyword.INCLUDE.getQName();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      assert phase == BuildPhase.LINKAGE;

      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      YangContext context = this.getContext();
      YangSchemaContext schemaContext = context.getSchemaContext();
      org.yangcentral.yangkit.model.api.stmt.Module curModule = context.getCurModule();
      boolean notFound = false;
      boolean wrongType = false;
      boolean incompatiableVersion = false;
      ValidatorRecordBuilder validatorRecordBuilder;
      this.includeModule = null;
      if (this.revisionDate == null) {
         List<org.yangcentral.yangkit.model.api.stmt.Module> moduleList = schemaContext.getModule(this.getArgStr());
         if (null != moduleList && moduleList.size() != 0) {
            if (moduleList.size() > 1) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.TOO_MANY_DEPENDECE_MODULES.getFieldName()));
               return validatorResultBuilder.build();
            }

            if (!(moduleList.get(0) instanceof SubModule)) {
               wrongType = true;
            } else {
               this.includeModule = (SubModule)moduleList.get(0);
            }
         } else {
            notFound = true;
         }
      } else {
         Optional<Module> moduleOp = schemaContext.getModule(this.getArgStr(), this.revisionDate.getArgStr());
         if (!moduleOp.isPresent()) {
            notFound = true;
         } else if (!(moduleOp.get() instanceof SubModule)) {
            wrongType = true;
         } else {
            this.includeModule = (SubModule)moduleOp.get();
         }
      }

      if (notFound) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + this.getArgStr()})));
         return validatorResultBuilder.build();
      }

      if (wrongType) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.WRONG_TYPE_DEPENDECE_MODULE.getFieldName() + " It should be a submodule, but get a module."));
         return validatorResultBuilder.build();
      }
      ValidatorResult includeResult = this.includeModule.init();
      if (!includeResult.isOk()) {
         if(schemaContext.isImportOnly(includeModule)){
            validatorResultBuilder.merge(includeResult);
         } else {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INCLUDE_MODULE_NOT_VALIDATE.toString(new String[]{"module=" + this.getArgStr()}) ));
         }
         return validatorResultBuilder.build();
      }
      if (!this.includeModule.getEffectiveYangVersion().equals(this.getContext().getCurModule().getEffectiveYangVersion())) {
         incompatiableVersion = true;
      }

      if (incompatiableVersion) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 Severity.WARNING,ErrorTag.BAD_ELEMENT,
                 ErrorCode.INCOMPATIBLE_YANG_VERSION.getFieldName()));
      }

      boolean includedErrorSubmodule = false;
      if (curModule instanceof MainModule) {
         if (!curModule.getArgStr().equals(this.includeModule.getBelongsto().getArgStr())) {
            includedErrorSubmodule = true;
         }
      } else {
         SubModule sb = (SubModule)curModule;
         if (!sb.getBelongsto().getArgStr().equals(this.includeModule.getBelongsto().getArgStr())) {
            includedErrorSubmodule = true;
         }
      }

      if (includedErrorSubmodule) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INCLUDE_MODULE_NOT_BELONGSTO_SELF.getFieldName()));
         return validatorResultBuilder.build();
      }

      if (this.getContext().getCurModule().getEffectiveYangVersion().equals(Yang.VERSION_1)) {
         includeResult = this.includeModule.build();
         if (!includeResult.isOk()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INCLUDE_MODULE_NOT_VALIDATE.toString(new String[]{"module=" + this.getArgStr()}) ));
         }
      }

      return validatorResultBuilder.build();

   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() != 0) {
         this.description = (Description)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() != 0) {
         this.reference = (Reference)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REVISIONDATE.getQName());
      if (matched.size() != 0) {
         this.revisionDate = (RevisionDate)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
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
