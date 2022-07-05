package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.BitsStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BitsStringValueCodecImpl extends StringValueCodecImpl<List<String>> implements BitsStringValueCodec {
   public List<String> deserialize(Restriction<List<String>> restriction, String input) throws YangCodecException {
      String[] splitStr = input.split(" ");
      List<String> ret = new ArrayList();
      String[] var5 = splitStr;
      int var6 = splitStr.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String str = var5[var7];
         str = str.trim();
         if (str.length() > 0) {
            ret.add(str);
         }
      }

      boolean bool = restriction.evaluated(ret);
      if (!bool) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         return ret;
      }
   }

   public String serialize(Restriction<List<String>> restriction, List<String> output) throws YangCodecException {
      boolean bool = restriction.evaluated(output);
      if (!bool) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         StringBuffer sb = new StringBuffer();
         Iterator var5 = output.iterator();

         while(var5.hasNext()) {
            String str = (String)var5.next();
            sb.append(str);
            sb.append(" ");
         }

         return sb.toString().trim();
      }
   }
}
