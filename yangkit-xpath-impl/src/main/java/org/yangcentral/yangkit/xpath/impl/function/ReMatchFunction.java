package org.yangcentral.yangkit.xpath.impl.function;

import org.apache.xerces.impl.xpath.regex.RegularExpression;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class ReMatchFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && args.size() == 2) {
         String candidate = (String)args.get(0);
         String pattern = (String)args.get(1);
         try {
            RegularExpression regPattern = new RegularExpression(pattern, "X");
            return regPattern.matches(candidate);
         } catch (RuntimeException e) {
            throw new FunctionCallException("invalid XML Schema regex pattern: " + pattern, e);
         }
      } else {
         throw new FunctionCallException("re-match function MUST have 2 arguments.");
      }
   }
}
