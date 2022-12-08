package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.Location;

import java.util.Objects;

class XPathLocation implements Location<String> {
   private String xpath;

   public XPathLocation(String xpath) {
      this.xpath = xpath;
   }

   public String getLocation() {
      return this.xpath;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      XPathLocation that = (XPathLocation) o;
      return xpath.equals(that.xpath);
   }

   @Override
   public int hashCode() {
      return Objects.hash(xpath);
   }
}
