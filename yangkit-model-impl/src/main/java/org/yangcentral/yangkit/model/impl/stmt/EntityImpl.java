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
   private StatusStmt status;
   private Description description;
   private Reference reference;

   public EntityImpl(String argStr) {
      super(argStr);
   }

   public StatusStmt getStatus() {
      return this.status;
   }

   public Status getEffectiveStatus() {
      return null == this.status ? Status.CURRENT : Status.getStatus(this.status.getArgStr());
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

   @Override
   protected void clearSelf() {
      this.description = null;
      this.reference = null;
      this.status = null;
      super.clearSelf();
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

      matched = this.getSubStatement(YangBuiltinKeyword.STATUS.getQName());
      if (matched.size() != 0) {
         this.status = (StatusStmt)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      if (this.status != null) {
         statements.add(this.status);
      } else {
         StatusStmt newStatus = new StatusImpl("current");
         newStatus.setContext(new YangContext(this.getContext()));
         newStatus.setElementPosition(this.getElementPosition());
         newStatus.setParentStatement(this);
         newStatus.init();
         statements.add(newStatus);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
