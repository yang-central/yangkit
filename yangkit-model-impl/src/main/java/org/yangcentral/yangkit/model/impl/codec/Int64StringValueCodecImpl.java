package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.Int64StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class Int64StringValueCodecImpl extends StringValueCodecImpl<Long> implements Int64StringValueCodec {
   public Long deserialize(Restriction<Long> restriction, String input) throws YangCodecException {
      Long l = Long.valueOf(input);
      if (!restriction.evaluated(l)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return l;
      }
   }

   public String serialize(Restriction<Long> restriction, Long output) throws YangCodecException {
      if (!restriction.evaluated(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
