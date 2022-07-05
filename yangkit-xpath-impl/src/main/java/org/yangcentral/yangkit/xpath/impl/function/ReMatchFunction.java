package org.yangcentral.yangkit.xpath.impl.function;

import java.util.List;
import java.util.regex.Pattern;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class ReMatchFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      if (args != null && args.size() == 2) {
         String candidate = (String)args.get(0);
         String pattern = (String)args.get(1);
         Pattern regPattern = Pattern.compile(pattern);
         return regPattern.matcher(candidate).matches();
      } else {
         throw new FunctionCallException("re-match function MUST have 2 arguments.");
      }
   }
}
