package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.ErrorAppTagStmt;

public class ErrorAppTagImpl extends YangSimpleStatementImpl implements ErrorAppTagStmt {
   public ErrorAppTagImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ERRORAPPTAG.getQName();
   }
}
