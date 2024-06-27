package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.Decimal64StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

import java.math.BigDecimal;

public class Decimal64StringValueCodecImpl extends StringValueCodecImpl<BigDecimal> implements Decimal64StringValueCodec {
   public BigDecimal deserialize(Restriction<BigDecimal> restriction, String input) throws YangCodecException {
      BigDecimal bigDecimal;
      try {
         bigDecimal = new BigDecimal(input);
      } catch (NumberFormatException e) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }
      if (!restriction.evaluate(bigDecimal)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return bigDecimal;
      }
   }

   public String serialize(Restriction<BigDecimal> restriction, BigDecimal output) throws YangCodecException {
      if (!restriction.evaluate(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
