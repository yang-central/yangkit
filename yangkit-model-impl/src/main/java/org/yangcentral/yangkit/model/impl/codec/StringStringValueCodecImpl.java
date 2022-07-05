package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.StringStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class StringStringValueCodecImpl extends StringValueCodecImpl<String> implements StringStringValueCodec {
   public String deserialize(Restriction<String> restriction, String input) throws YangCodecException {
      if (!restriction.evaluated(input)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return input;
      }
   }

   public String serialize(Restriction<String> restriction, String output) throws YangCodecException {
      if (!restriction.evaluated(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return output;
      }
   }
}
