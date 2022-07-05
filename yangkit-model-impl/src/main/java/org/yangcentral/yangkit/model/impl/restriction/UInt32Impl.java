package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.UInt32;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class UInt32Impl extends YangIntegerImpl<Long> implements UInt32 {
   public UInt32Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public UInt32Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof UInt32) ? false : super.equals(obj);
   }
}
