package org.yangcentral.yangkit.xpath.impl.function;

import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Bits;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class BitIsSetFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && args.size() == 2) {
         List nodeSet = (List)args.get(0);
         if (nodeSet.isEmpty()) {
            throw new FunctionCallException("bit-is-set function's first argument is a empty nodeset.");
         } else {
            YangData<?> node = (YangData)nodeSet.get(0);
            if (!(node instanceof TypedData)) {
               return false;
            } else {
               TypedData typedData = (TypedData)node;
               if (!(((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction() instanceof Bits)) {
                  return false;
               } else {
                  List<String> value = null;
                  try {
                     value = (List)(typedData.getValue().getValue());
                  } catch (YangCodecException e) {
                     throw new FunctionCallException(e);
                  }
                  String bitName = (String)args.get(1);
                  return value.contains(bitName);
               }
            }
         }
      } else {
         throw new FunctionCallException("bit-is-set function MUST have 2 arguments.");
      }
   }
}
