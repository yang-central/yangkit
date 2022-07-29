package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Argument;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class ExtensionImpl extends EntityImpl implements Extension {
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

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      this.argument = null;
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.ARGUMENT.getQName());
      if (matched.size() > 0) {
         this.argument = (Argument)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.argument != null) {
         statements.add(this.argument);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
