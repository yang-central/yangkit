package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.UInt16;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class Uint16Impl extends YangIntegerImpl<Integer> implements UInt16 {
   public Uint16Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Uint16Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof UInt16) ? false : super.equals(obj);
   }
}
