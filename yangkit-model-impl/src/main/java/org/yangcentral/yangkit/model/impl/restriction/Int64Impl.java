package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Int64;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class Int64Impl extends YangIntegerImpl<Long> implements Int64 {
   public Int64Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Int64Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof Int64) ? false : super.equals(obj);
   }
}
