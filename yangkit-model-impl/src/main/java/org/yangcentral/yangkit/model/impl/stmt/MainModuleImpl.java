package org.yangcentral.yangkit.model.impl.stmt;

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
import org.yangcentral.yangkit.model.api.stmt.Include;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Namespace;
import org.yangcentral.yangkit.model.api.stmt.Prefix;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainModuleImpl extends ModuleImpl implements MainModule {
   private Namespace namespace;
   private Prefix prefix;

   public MainModuleImpl(String argStr) {
      super(argStr);
   }

   public Namespace getNamespace() {
      return this.namespace;
   }

   public Prefix getPrefix() {
      return this.prefix;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MODULE.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.NAMESPACE.getQName());
      if (matched.size() > 0) {
         this.namespace = (Namespace)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.PREFIX.getQName());
      if (matched.size() > 0) {
         this.prefix = (Prefix)matched.get(0);
         if (this.getPrefixes().containsKey(this.prefix.getArgStr())) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this.prefix);
            validatorRecordBuilder.setErrorPath(this.prefix.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         }

         this.prefixCache.put(this.prefix.getArgStr(), this.getModuleId());
      }

      return validatorResultBuilder.build();
   }

   public boolean isSelfPrefix(String prefix) {
      return this.getPrefix().getArgStr().equals(prefix);
   }

   public String getSelfPrefix() {
      return this.getPrefix().getArgStr();
   }

   public MainModule getMainModule() {
      return this;
   }

   public List<YangStatement> getEffectiveMetaStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.namespace != null) {
         statements.add(this.namespace);
      }

      if (this.prefix != null) {
         statements.add(this.prefix);
      }

      statements.addAll(super.getEffectiveMetaStatements());
      return statements;
   }

   public List<YangStatement> getEffectiveDefinitionStatement() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(super.getEffectiveDefinitionStatement());
      Iterator var2 = this.getIncludes().iterator();

      while(var2.hasNext()) {
         Include include = (Include)var2.next();
         if (include.getInclude().isPresent()) {
            SubModule subModule = (SubModule)include.getInclude().get();
            statements.addAll(subModule.getAugments());
         }
      }

      return statements;
   }
}
