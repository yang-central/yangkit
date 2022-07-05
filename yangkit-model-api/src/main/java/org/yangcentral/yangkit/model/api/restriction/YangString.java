package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Length;
import org.yangcentral.yangkit.model.api.stmt.type.Pattern;

import java.math.BigInteger;
import java.util.List;

public interface YangString extends Restriction<String> {
   BigInteger MAX_LENGTH = new BigInteger("18446744073709551615");
   BigInteger MIN_LENGTH = BigInteger.ZERO;

   Length getLength();

   Length getEffectiveLength();

   List<Pattern> getPatterns();

   List<Pattern> getEffectivePatterns();

   BigInteger getMaxLength();

   BigInteger getMinLength();

   ValidatorResult setLength(Length var1);
}
