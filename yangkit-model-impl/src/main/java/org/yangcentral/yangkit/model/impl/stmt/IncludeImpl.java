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
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Include;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.RevisionDate;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

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
      if (this.revisionDate == null) {
         List<org.yangcentral.yangkit.model.api.stmt.Module> moduleList = schemaContext.getModule(this.getArgStr());
         if (null != moduleList && moduleList.size() != 0) {
            if (moduleList.size() > 1) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.TOO_MANY_DEPENDECE_MODULES.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + this.getArgStr()})));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else if (wrongType) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.WRONG_TYPE_DEPENDECE_MODULE.getFieldName() + " It should be a submodule, but get a module."));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         if (!this.includeModule.isInit()) {
            if (!schemaContext.isImportOnly(this.includeModule)) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INCLUDE_MODULE_NOT_VALIDATE.toString(new String[]{"module=" + this.getArgStr()})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            }

            ValidatorResult includeResult = this.includeModule.init();
            if (!includeResult.isOk()) {
               validatorResultBuilder.merge(includeResult);
               return validatorResultBuilder.build();
            }
         }

         if (!this.includeModule.getEffectiveYangVersion().equals(this.getContext().getCurModule().getEffectiveYangVersion())) {
            incompatiableVersion = true;
         }

         if (incompatiableVersion) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(Severity.WARNING);
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INCOMPATIBLE_YANG_VERSION.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INCLUDE_MODULE_NOT_BELONGSTO_SELF.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         } else {
            if (this.getContext().getCurModule().getEffectiveYangVersion().equals("1")) {
               ValidatorResult includeResult = this.includeModule.build();
               if (!includeResult.isOk()) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INCLUDE_MODULE_NOT_VALIDATE.toString(new String[]{"module=" + this.getArgStr()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            return validatorResultBuilder.build();
         }
      }
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
