package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Argument;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YinElement;

import java.util.ArrayList;
import java.util.List;

public class ArgumentImpl extends YangBuiltInStatementImpl implements Argument {
   private YinElement yinElement;

   public ArgumentImpl(String argStr) {
      super(argStr);
   }

   public boolean isYinElement() {
      return this.yinElement == null ? false : this.yinElement.value();
   }

   public YinElement getYinElement() {
      return this.yinElement;
   }

   @Override
   protected void clearSelf() {
      yinElement = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.YINELEMENT.getQName());

      if (matched.size() > 0) {
         this.yinElement = (YinElement)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ARGUMENT.getQName();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.yinElement != null) {
         statements.add(this.yinElement);
      } else {
         YinElement newYinElement = new YinElementImpl("false");
         newYinElement.setContext(new YangContext(this.getContext()));
         newYinElement.setElementPosition(this.getElementPosition());
         newYinElement.setParentStatement(this);
         newYinElement.init();
         statements.add(newYinElement);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
