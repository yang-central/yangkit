package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Default;

public class DefaultImpl extends YangSimpleStatementImpl implements Default {
   private Object value;

   public DefaultImpl(String argStr) {
      super(argStr);
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.DEFAULT.getQName();
   }
}
