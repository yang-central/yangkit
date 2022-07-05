package org.yangcentral.yangkit.xpath.impl.function;

import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.restriction.Enumeration;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class EnumValueFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && args.size() == 1) {
         List nodeSet = (List)args.get(0);
         if (nodeSet.isEmpty()) {
            throw new FunctionCallException("enum-value function's first argument is a empty nodeset.");
         } else {
            YangData<?> node = (YangData)nodeSet.get(0);
            if (!(node instanceof TypedData)) {
               return "NaN";
            } else {
               TypedData typedData = (TypedData)node;
               if (!(((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction() instanceof Enumeration)) {
                  return "NaN";
               } else {
                  Enumeration enumeration = (Enumeration)((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction();
                  return enumeration.getEnumActualValue(typedData.getStringValue());
               }
            }
         }
      } else {
         throw new FunctionCallException("enum-value function MUST have one argument.");
      }
   }
}
