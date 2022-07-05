package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.model.api.codec.BooleanStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public class BooleanStringValueCodecImpl extends StringValueCodecImpl<Boolean> implements BooleanStringValueCodec {
   public Boolean deserialize(Restriction<Boolean> restriction, String input) throws YangCodecException {
      return Boolean.valueOf(input);
   }

   public String serialize(Restriction<Boolean> restriction, Boolean output) throws YangCodecException {
      return output.toString();
   }
}
