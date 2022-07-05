package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Int8;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class Int8Impl extends YangIntegerImpl<Byte> implements Int8 {
   public Int8Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Int8Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof Int8) ? false : super.equals(obj);
   }
}
