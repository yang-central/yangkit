package org.yangcentral.yangkit.parser;

import java.net.URI;

public class Capability {
   private URI uri;

   public Capability(URI uri) {
      this.uri = uri;
   }

   public URI getUri() {
      return this.uri;
   }

   public String toString() {
      return this.uri.toString();
   }
}
