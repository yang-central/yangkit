package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.ErrorMessageStmt;

public class ErrorMessageImpl extends YangSimpleStatementImpl implements ErrorMessageStmt {
   public ErrorMessageImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ERRORMESSAGE.getQName();
   }
}
