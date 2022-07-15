package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.xpath.YangLocationPath;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;
import org.jaxen.JaxenException;

public class PathImpl extends YangBuiltInStatementImpl implements Path {
   private YangXPath path;

   public PathImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.PATH.getQName();
   }

   public YangXPath getXPathExpression() {
      return this.path;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      ValidatorRecordBuilder validatorRecordBuilder;
      try {
         YangXPath xPath = new YangXPathImpl(this.getArgStr());
         if (!(xPath.getRootExpr() instanceof YangLocationPath)) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }

         this.path = xPath;
      } catch (JaxenException e) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_XPATH.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      return validatorResultBuilder.build();
   }
}
