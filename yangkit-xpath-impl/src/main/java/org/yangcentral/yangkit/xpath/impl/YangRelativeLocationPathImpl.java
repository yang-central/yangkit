package org.yangcentral.yangkit.xpath.impl;

public class YangRelativeLocationPathImpl extends YangLocationPathImpl {
   public boolean isAbsolute() {
      return false;
   }

   public String toString() {
      return "[(YangRelativeLocationPath): " + super.toString() + "]";
   }
}
