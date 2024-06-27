package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.BinaryStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Binary;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

import java.util.Base64;

public class BinaryStringValueCodecImpl extends StringValueCodecImpl<byte[]> implements BinaryStringValueCodec {
   public String serialize(Restriction<byte[]> restriction, byte[] output) throws YangCodecException {
      if (!(restriction instanceof Binary)) {
         throw new IllegalArgumentException("wrong restriction type");
      } else {
         boolean bool = restriction.evaluate(output);
         if (bool) {
            throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
         } else {
            return Base64.getEncoder().encodeToString(output);
         }
      }
   }

   public byte[] deserialize(Restriction<byte[]> restriction, String input) throws YangCodecException {
      byte[] decode;
      try {
         decode = Base64.getDecoder().decode(input);
      } catch (IllegalArgumentException e) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }

      boolean bool = restriction.evaluate(decode);
      if (!bool) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return decode;
      }
   }
}
