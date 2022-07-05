package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.UInt16StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class UInt16StringValueCodecImpl extends StringValueCodecImpl<Integer> implements UInt16StringValueCodec {
   public Integer deserialize(Restriction<Integer> restriction, String input) throws YangCodecException {
      Integer integer = Integer.valueOf(input);
      if (!restriction.evaluated(integer)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return integer;
      }
   }

   public String serialize(Restriction<Integer> restriction, Integer output) throws YangCodecException {
      if (!restriction.evaluated(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
