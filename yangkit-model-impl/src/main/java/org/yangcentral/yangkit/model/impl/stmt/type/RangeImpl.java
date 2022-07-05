package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.type.Range;

public class RangeImpl extends SectionExpressionImpl implements Range {
   public RangeImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.RANGE.getQName();
   }
}
