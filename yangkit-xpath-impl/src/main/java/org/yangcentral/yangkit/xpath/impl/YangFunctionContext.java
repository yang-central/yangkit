package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.xpath.impl.function.BitIsSetFunction;
import org.yangcentral.yangkit.xpath.impl.function.CurrentFunction;
import org.yangcentral.yangkit.xpath.impl.function.DeRefFunction;
import org.yangcentral.yangkit.xpath.impl.function.DerivedFromFunction;
import org.yangcentral.yangkit.xpath.impl.function.DerivedFromOrSelfFunction;
import org.yangcentral.yangkit.xpath.impl.function.EnumValueFunction;
import org.yangcentral.yangkit.xpath.impl.function.ReMatchFunction;
import org.jaxen.Function;
import org.jaxen.UnresolvableException;
import org.jaxen.XPathFunctionContext;

public class YangFunctionContext extends XPathFunctionContext {
   private static final YangFunctionContext YANG11_FUNCTION_CONTEXT = new YangFunctionContext();
   private static final YangFunctionContext YANG10_FUNCTION_CONTEXT = new YangFunctionContext();

   private YangFunctionContext() {
      super(false);
   }

   private YangFunctionContext(boolean includeExtensionFunctions) {
      super(includeExtensionFunctions);
   }

   public static YangFunctionContext getInstance(String yangVersion) {
      if (yangVersion.equals("1.1")) {
         YANG11_FUNCTION_CONTEXT.registerYang11Functions();
         return YANG11_FUNCTION_CONTEXT;
      } else if (yangVersion.equals("1")) {
         YANG10_FUNCTION_CONTEXT.registerYang10Functions();
         return YANG10_FUNCTION_CONTEXT;
      } else {
         throw new IllegalArgumentException("invalid yang-version");
      }
   }

   private void registerYang10Functions() {
      this.registerFunction((String)null, "current", new CurrentFunction());
   }

   private void registerYang11Functions() {
      this.registerFunction((String)null, "current", new CurrentFunction());
      this.registerFunction((String)null, "deref", new DeRefFunction());
      this.registerFunction((String)null, "re-match", new ReMatchFunction());
      this.registerFunction((String)null, "derived-from", new DerivedFromFunction());
      this.registerFunction((String)null, "derived-from-or-self", new DerivedFromOrSelfFunction());
      this.registerFunction((String)null, "enum-value", new EnumValueFunction());
      this.registerFunction((String)null, "bit-is-set", new BitIsSetFunction());
   }

   public Function getFunction(String namespaceURI, String prefix, String localName) throws UnresolvableException {
      return super.getFunction(namespaceURI, prefix, localName);
   }
}
