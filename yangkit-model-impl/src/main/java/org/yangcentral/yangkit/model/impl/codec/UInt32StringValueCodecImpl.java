package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.UInt32StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class UInt32StringValueCodecImpl extends StringValueCodecImpl<Long> implements UInt32StringValueCodec {
   public Long deserialize(Restriction<Long> restriction, String input) throws YangCodecException {
      Long l;
      try {
         l = Long.valueOf(input);
      } catch (NumberFormatException e) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }
      if (!restriction.evaluate(l)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return l;
      }
   }

   public String serialize(Restriction<Long> restriction, Long output) throws YangCodecException {
      if (!restriction.evaluate(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
