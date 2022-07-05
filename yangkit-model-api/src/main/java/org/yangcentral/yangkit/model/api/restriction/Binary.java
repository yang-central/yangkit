package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Length;

import java.math.BigInteger;

public interface Binary extends Restriction<byte[]> {
   Length getLength();

   Length getEffectiveLength();

   ValidatorResult setLength(Length var1);

   BigInteger getMaxLength();

   BigInteger getMinLength();
}
