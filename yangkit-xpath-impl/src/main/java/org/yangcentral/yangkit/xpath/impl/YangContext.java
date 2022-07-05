package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.Context;
import org.yangcentral.yangkit.xpath.YangContextSupport;

public class YangContext extends Context {
   private YangContextSupport yangContextSupport;

   public YangContext(YangContextSupport contextSupport) {
      super(contextSupport);
      this.yangContextSupport = contextSupport;
   }

   public void setContextSupport(YangContextSupport contextSupport) {
      super.setContextSupport(contextSupport);
      this.yangContextSupport = contextSupport;
   }

   public YangContextSupport getContextSupport() {
      return this.yangContextSupport;
   }
}
