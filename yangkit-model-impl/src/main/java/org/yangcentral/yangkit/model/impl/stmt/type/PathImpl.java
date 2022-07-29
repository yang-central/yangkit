package org.yangcentral.yangkit.model.impl.stmt.type;

import org.jaxen.JaxenException;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangLocationPath;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathPrefixVisitor;

import java.util.List;

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
      try {
         YangXPath xPath = new YangXPathImpl(this.getArgStr());
         if (!(xPath.getRootExpr() instanceof YangLocationPath)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_XPATH.getFieldName()));
         }

         this.path = xPath;
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
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_XPATH.getFieldName()));
      }

      return validatorResultBuilder.build();
   }
}
