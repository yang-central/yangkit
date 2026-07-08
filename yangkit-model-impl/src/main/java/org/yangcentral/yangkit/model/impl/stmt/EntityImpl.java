package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.Status;
import org.yangcentral.yangkit.model.api.stmt.StatusStmt;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityImpl extends YangStatementImpl implements Entity {
   private final EntitySupport entitySupport = new EntitySupport();

   public EntityImpl(String argStr) {
      super(argStr);
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
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() != 0) {
         this.entitySupport.setDescription((Description)matched.get(0));
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() != 0) {
         this.entitySupport.setReference((Reference)matched.get(0));
      }

      matched = this.getSubStatement(YangBuiltinKeyword.STATUS.getQName());
      if (matched.size() != 0) {
         this.entitySupport.setStatus((StatusStmt)matched.get(0));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      statements.addAll(this.entitySupport.getEffectiveSubStatements(this));
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
