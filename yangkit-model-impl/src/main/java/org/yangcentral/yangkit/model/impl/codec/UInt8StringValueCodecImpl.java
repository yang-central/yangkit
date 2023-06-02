package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.UInt8StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class UInt8StringValueCodecImpl extends StringValueCodecImpl<Short> implements UInt8StringValueCodec {
   public Short deserialize(Restriction<Short> restriction, String input) throws YangCodecException {
      Short s;
      try {
         s = Short.valueOf(input);
      } catch (NumberFormatException e) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }
      if (!restriction.evaluated(s)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return s;
      }
   }

   public String serialize(Restriction<Short> restriction, Short output) throws YangCodecException {
      if (!restriction.evaluated(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
