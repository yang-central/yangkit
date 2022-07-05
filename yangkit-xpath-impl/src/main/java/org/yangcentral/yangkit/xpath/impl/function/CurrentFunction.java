package org.yangcentral.yangkit.xpath.impl.function;

import org.yangcentral.yangkit.xpath.YangContextSupport;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class CurrentFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && !args.isEmpty()) {
         throw new FunctionCallException("current() MUST no argument");
      } else {
         YangContextSupport contextSupport = (YangContextSupport)context.getContextSupport();
         if (contextSupport.getContextData() == null) {
            throw new FunctionCallException("context node is not exist.");
         } else {
            return contextSupport.getContextData();
         }
      }
   }
}
