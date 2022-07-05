package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.ErrorAppTagStmt;
import org.yangcentral.yangkit.model.api.stmt.ErrorMessageStmt;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.Modifier;
import org.yangcentral.yangkit.model.api.stmt.type.Pattern;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;

import java.util.ArrayList;
import java.util.List;

public class PatternImpl extends YangBuiltInStatementImpl implements Pattern {
   private ErrorMessageStmt errorMessage;
   private ErrorAppTagStmt errorAppTag;
   private Description description;
   private Reference reference;
   private java.util.regex.Pattern pattern;
   private Modifier modifier;

   public PatternImpl(String argStr) {
      super(argStr);
   }

   public ErrorAppTagStmt getErrorAppTag() {
      return this.errorAppTag;
   }

   public ErrorMessageStmt getErrorMessage() {
      return this.errorMessage;
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
      return YangBuiltinKeyword.PATTERN.getQName();
   }

   public java.util.regex.Pattern getPattern() {
      return this.pattern;
   }

   public Modifier getModifier() {
      return this.modifier;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      try {
         String fixedName = this.getArgStr().replaceAll("\\\\p\\{IsBasicLatin\\}", "\\\\p\\{InBasicLatin\\}");
         fixedName = fixedName.replaceAll("\\\\p\\{IsLatin-1Supplement\\}", "\\\\p\\{InLatin-1Supplement\\}");
         this.pattern = java.util.regex.Pattern.compile(fixedName);
      } catch (RuntimeException var4) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_PATTERN.toString(new String[]{"name=" + this.getArgStr()})));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() != 0) {
         this.description = (Description)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() != 0) {
         this.reference = (Reference)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.ERRORMESSAGE.getQName());
      if (matched.size() != 0) {
         this.errorMessage = (ErrorMessageStmt)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.ERRORAPPTAG.getQName());
      if (matched.size() != 0) {
         this.errorAppTag = (ErrorAppTagStmt)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.MODIFIER.getQName());
      if (matched.size() != 0) {
         this.modifier = (Modifier)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Pattern)) {
         return false;
      } else {
         Pattern another = (Pattern)obj;
         if (another.getModifier() != null && this.getModifier() != null) {
            if (!another.getModifier().equals(this.getModifier())) {
               return false;
            }
         } else if (another.getModifier() != null || this.getModifier() != null) {
            return false;
         }

         return super.equals(obj);
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.errorMessage != null) {
         statements.add(this.errorMessage);
      }

      if (this.errorAppTag != null) {
         statements.add(this.errorAppTag);
      }

      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      if (this.modifier != null) {
         statements.add(this.modifier);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
