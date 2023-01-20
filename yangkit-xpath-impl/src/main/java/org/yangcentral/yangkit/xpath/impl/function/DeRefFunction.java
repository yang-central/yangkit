package org.yangcentral.yangkit.xpath.impl.function;

import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.xpath.impl.YangAbsoluteLocationPathImpl;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.xpath.YangXPath;
import java.util.Collections;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenException;

public class DeRefFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && args.size() == 1) {
         List nodeSet = (List)args.get(0);
         if (nodeSet.isEmpty()) {
            throw new FunctionCallException("deref function's first argument is a empty nodeset.");
         } else {
            YangData<?> node = (YangData)nodeSet.get(0);
            if (!(node instanceof TypedData)) {
               return Collections.EMPTY_LIST;
            } else {
               TypedData typedData = (TypedData)node;
               if (!(((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction() instanceof LeafRef) && !(((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction() instanceof InstanceIdentifier)) {
                  return Collections.EMPTY_LIST;
               } else if (((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction() instanceof LeafRef) {
                  LeafRef leafRef = (LeafRef)((TypedDataNode)typedData.getSchemaNode()).getType().getRestriction();
                  YangXPath yangXPath = leafRef.getEffectivePath().getXPathExpression();

                  try {
                     if(yangXPath != null) {
                        return yangXPath.evaluate(node);
                     } else {
                        return Collections.EMPTY_LIST;
                     }
                  } catch (JaxenException var9) {
                     return Collections.EMPTY_LIST;
                  }
               } else {
                  YangAbsoluteLocationPathImpl yangAbsoluteLocationPath = (YangAbsoluteLocationPathImpl)typedData.getValue();

                  try {
                     return yangAbsoluteLocationPath.evaluate(context);
                  } catch (JaxenException var10) {
                     return Collections.EMPTY_LIST;
                  }
               }
            }
         }
      } else {
         throw new FunctionCallException("deref function MUST have 1 argument.");
      }
   }
}
