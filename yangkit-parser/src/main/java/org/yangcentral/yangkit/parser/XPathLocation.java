package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.Location;

class XPathLocation implements Location<String> {
   private String xpath;

   public XPathLocation(String xpath) {
      this.xpath = xpath;
   }

   public String getLocation() {
      return this.xpath;
   }
}
