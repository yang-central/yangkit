package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;
import java.util.ArrayList;
import java.util.List;
import org.jaxen.JaxenException;
import org.yangcentral.yangkit.xpath.impl.YangXPathPrefixVisitor;

public class MustImpl extends YangBuiltInStatementImpl implements Must {
   private ErrorMessageStmt errorMessage;
   private ErrorAppTagStmt errorAppTag;
   private Description description;
   private Reference reference;
   private YangXPath expression;

   public MustImpl(String argStr) {
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

   public YangXPath getXPathExpression() {
      return this.expression;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.MUST.getQName();
   }

   @Override
   protected void clearSelf() {
      this.description = null;
      this.reference = null;
      this.errorMessage = null;
      this.errorAppTag = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      try {
         YangXPath xPath = new YangXPathImpl(this.getArgStr());
         this.expression = xPath;
         Module curModule = this.getContext().getCurModule();
         YangXPathPrefixVisitor pathPrefixVisitor = new YangXPathPrefixVisitor(this,curModule);
         List<String> prefixes = pathPrefixVisitor.visit(xPath.getRootExpr(),this);
         if(!prefixes.isEmpty()){
            for(String prefix:prefixes){
               Import im = curModule.getImportByPrefix(prefix);
               if(im !=null){
                  im.addReference(this);
               }
            }
         }
      } catch (JaxenException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_XPATH.getFieldName()));
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

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
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

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
   @Override
   public boolean equals(Object obj) {
      if(obj == null){
         return false;
      }
      if(!(obj instanceof Must)){
         return false;
      }
      if(this == obj){
         return true;
      }
      Must another = (Must) obj;
      return this.getXPathExpression().equals(another.getXPathExpression());
   }
}
