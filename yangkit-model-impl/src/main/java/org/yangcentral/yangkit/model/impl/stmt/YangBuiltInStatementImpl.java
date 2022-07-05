package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

public abstract class YangBuiltInStatementImpl extends YangStatementImpl {
   public YangBuiltInStatementImpl(String argStr) {
      super(argStr);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof YangBuiltinStatement)) {
         return false;
      } else if (!this.getYangKeyword().equals(((YangBuiltinStatement)obj).getYangKeyword())) {
         return false;
      } else if (this.getArgStr() != null && ((YangBuiltinStatement)obj).getArgStr() != null && this.getArgStr().equals(((YangBuiltinStatement)obj).getArgStr())) {
         return true;
      } else {
         return this.getArgStr() == null && ((YangBuiltinStatement)obj).getArgStr() == null;
      }
   }
}
