package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Include;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Namespace;
import org.yangcentral.yangkit.model.api.stmt.Prefix;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
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

   @Override
   protected void clearSelf() {
      this.namespace = null;
      if(this.prefix != null){
         prefixCache.remove(this.prefix.getArgStr());
      }
      this.prefix = null;
      super.clearSelf();
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
            validatorResultBuilder.addRecord(ModelUtil.reportError(prefix,
                    ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            return validatorResultBuilder.build();
         }

         this.prefixCache.put(this.prefix.getArgStr(), this.getModuleId());
      }

      return validatorResultBuilder.build();
   }

   public boolean isSelfPrefix(String prefix) {
      String selfPrefix = getSelfPrefix();
      if(selfPrefix == null){
         assert false;
         return false;
      }
      return selfPrefix.equals(prefix);

   }

   public String getSelfPrefix() {
      return this.getPrefix().getArgStr();
   }

   public MainModule getMainModule() {
      return this;
   }

   public List<YangStatement> getEffectiveMetaStatements() {
      List<YangStatement> statements = new ArrayList<>();
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
      List<YangStatement> statements = new ArrayList<>(super.getEffectiveDefinitionStatement());

      for (Include include : this.getIncludes()) {
         if (include.getInclude().isPresent()) {
            SubModule subModule = include.getInclude().get();
            statements.addAll(subModule.getAugments());
         }
      }

      return statements;
   }
}
