package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.ArrayList;
import java.util.List;

public class ExtensionImpl extends YangBuiltInStatementImpl implements Extension {
   private final EntitySupport entitySupport = new EntitySupport();
   private Argument argument;

   public ExtensionImpl(String argStr) {
      super(argStr);
   }

   public Argument getArgument() {
      return this.argument;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.EXTENSION.getQName();
   }

   public StatusStmt getStatus() {
      return this.entitySupport.getStatus();
   }

   public Status getEffectiveStatus() {
      return this.entitySupport.getEffectiveStatus();
   }

   public Description getDescription() {
      return this.entitySupport.getDescription();
   }

   public void setDescription(Description description) {
      this.entitySupport.setDescription(description);
   }

   public Reference getReference() {
      return this.entitySupport.getReference();
   }

   public void setReference(Reference reference) {
      this.entitySupport.setReference(reference);
   }

   @Override
   protected void clearSelf() {
      this.entitySupport.clear();
      this.argument = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      validatorResultBuilder.merge(this.entitySupport.init(this));

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.ARGUMENT.getQName());
      if (matched.size() > 0) {
         this.argument = (Argument)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      if (this.argument != null) {
         statements.add(this.argument);
      }

      statements.addAll(this.entitySupport.getEffectiveSubStatements(this));
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
