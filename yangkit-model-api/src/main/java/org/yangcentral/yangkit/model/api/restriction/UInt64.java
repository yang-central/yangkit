package org.yangcentral.yangkit.model.api.restriction;

import java.math.BigInteger;

public interface UInt64 extends YangInteger<BigInteger> {
   BigInteger MAX = new BigInteger("18446744073709551615");
   BigInteger MIN = BigInteger.ZERO;
}
