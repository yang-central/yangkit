package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Int32;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class Int32Impl extends YangIntegerImpl<Integer> implements Int32 {
   public Int32Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Int32Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof Int32) ? false : super.equals(obj);
   }
}
