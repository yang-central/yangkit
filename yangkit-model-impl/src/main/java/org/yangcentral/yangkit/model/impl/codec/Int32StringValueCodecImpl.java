package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.Int32StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class Int32StringValueCodecImpl extends StringValueCodecImpl<Integer> implements Int32StringValueCodec {
   public Integer deserialize(Restriction<Integer> restriction, String input) throws YangCodecException {
      Integer integer;
      try {
         integer = Integer.valueOf(input);
      } catch (NumberFormatException e) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }
      if (!restriction.evaluate(integer)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return integer;
      }
   }

   public String serialize(Restriction<Integer> restriction, Integer output) throws YangCodecException {
      if (!restriction.evaluate(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output.toString();
      }
   }
}
