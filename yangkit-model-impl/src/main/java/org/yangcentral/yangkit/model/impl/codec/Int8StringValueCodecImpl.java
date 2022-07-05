package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.Int8StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class Int8StringValueCodecImpl extends StringValueCodecImpl<Byte> implements Int8StringValueCodec {
   public Byte deserialize(Restriction<Byte> restriction, String input) throws YangCodecException {
      Byte b = Byte.valueOf(input);
      if (!restriction.evaluated(b)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return b;
      }
   }

   public String serialize(Restriction<Byte> restriction, Byte output) throws YangCodecException {
      if (!restriction.evaluated(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
