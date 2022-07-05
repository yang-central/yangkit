package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Int16;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class Int16Impl extends YangIntegerImpl<Short> implements Int16 {
   public Int16Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Int16Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof Int16) ? false : super.equals(obj);
   }
}
