package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultYangUnknown extends YangStatementImpl implements YangUnknown {
   private String keyword;
   private Extension extension;

   public DefaultYangUnknown(String keyword, String argStr) {
      super(argStr);
      this.keyword = keyword;
   }

   public String getKeyword() {
      return this.keyword;
   }

   public Extension getExtension() {
      return this.extension;
   }

   private Extension findExtensionFromKeyword(YangSchemaContext schemaContext, Module curModule) throws ModelException {
      String[] pair = this.keyword.split(":");
      if (pair.length != 2) {
         throw new ModelException(Severity.ERROR, this, ErrorCode.WRONG_USING_EXTENSION.getFieldName());
      } else {
         String prefix = pair[0];
         String extensionName = pair[1];
         Import im = curModule.getImportByPrefix(prefix);
         if (im != null) {
            im.setReferenced(true);
         }

         if (curModule.isSelfPrefix(prefix)) {
            Extension extension = curModule.getExtension(extensionName);
            if (extension != null) {
               return extension;
            }
         }

         Optional<ModuleId> optionalModule = curModule.findModuleByPrefix(prefix);
         if (!optionalModule.isPresent()) {
            return null;
         } else {
            Optional<Module> impModuleOp = schemaContext.getModule((ModuleId)optionalModule.get());
            if (!impModuleOp.isPresent()) {
               return null;
            } else {
               Module impModule = (Module)impModuleOp.get();
               return impModule.getExtension(extensionName);
            }
         }
      }
   }

   private ValidatorResult validate_grammar() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.extension != null) {
         return validatorResultBuilder.build();
      } else {
         ValidatorRecordBuilder validatorRecordBuilder;
         try {
            Extension extension = this.findExtensionFromKeyword(this.getContext().getSchemaContext(), this.getContext().getCurModule());
            if (null != extension) {
               this.extension = extension;
            }

            if (null == extension) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNKNOWN_EXTENSION.getFieldName()));
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            }
         } catch (ModelException var4) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(var4.getSeverity());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(var4.getMessage()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         }

         return validatorResultBuilder.build();
      }
   }

   public QName getYangKeyword() {
      return Yang.UNKNOWN;
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      assert phase == BuildPhase.GRAMMAR;

      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      return this.isBuilt ? validatorResultBuilder.build() : this.validate_grammar();
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (this == o) {
         return true;
      } else if (!(o instanceof YangUnknown)) {
         return false;
      } else {
         YangUnknown that = (YangUnknown)o;
         if (!this.getKeyword().equals(that.getKeyword())) {
            return false;
         } else if (this.getArgStr() != null && that.getArgStr() != null && this.getArgStr().equals(that.getArgStr())) {
            return true;
         } else {
            return this.getArgStr() == null && that.getArgStr() == null;
         }
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getKeyword()});
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      Iterator var2 = this.getSubElements().iterator();

      while(var2.hasNext()) {
         YangElement element = (YangElement)var2.next();
         if (element instanceof YangStatement) {
            statements.add((YangStatement)element);
         }
      }

      return statements;
   }
}
