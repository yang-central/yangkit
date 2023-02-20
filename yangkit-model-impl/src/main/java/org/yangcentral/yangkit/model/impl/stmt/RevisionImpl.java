package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.Revision;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;

public class RevisionImpl extends YangSimpleStatementImpl implements Revision {
   private Description description;
   private Reference reference;

   public RevisionImpl(String argStr) {
      super(argStr);
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
      return YangBuiltinKeyword.REVISION.getQName();
   }

   @Override
   protected void clearSelf() {
      this.description = null;
      this.reference = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (!ModelUtil.isYYYY_MM_DD(this.getArgStr())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_REVISION_FORMAT.getFieldName()));
      }

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() > 0) {
         this.description = (Description)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() > 0) {
         this.reference = (Reference)matched.get(0);
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

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
