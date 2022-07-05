package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.EmptyStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.apache.commons.lang3.ObjectUtils;

public class EmptyStringValueCodecImpl extends StringValueCodecImpl<ObjectUtils.Null> implements EmptyStringValueCodec {
   public ObjectUtils.Null deserialize(Restriction<ObjectUtils.Null> restriction, String s) throws YangCodecException {
      if (s != null && s.length() <= 0) {
         return ObjectUtils.NULL;
      } else {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      }
   }

   public String serialize(Restriction<ObjectUtils.Null> restriction, ObjectUtils.Null s) throws YangCodecException {
      return "";
   }
}
