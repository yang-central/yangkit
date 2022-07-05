package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.UInt64;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

import java.math.BigInteger;

public class UInt64Impl extends YangIntegerImpl<BigInteger> implements UInt64 {
   public UInt64Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public UInt64Impl(YangContext context) {
      super(context);
   }

   public boolean equals(Object obj) {
      return !(obj instanceof UInt64) ? false : super.equals(obj);
   }
}
