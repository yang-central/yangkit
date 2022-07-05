package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.type.Length;

public class LengthImpl extends SectionExpressionImpl implements Length {
   public LengthImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.LENGTH.getQName();
   }
}
