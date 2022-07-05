package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.UInt8;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class UInt8Impl extends YangIntegerImpl<Short> implements UInt8 {
   public UInt8Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public UInt8Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof UInt8) ? false : super.equals(obj);
   }
}
