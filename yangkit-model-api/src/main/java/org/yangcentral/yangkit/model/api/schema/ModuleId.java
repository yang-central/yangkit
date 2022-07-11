package org.yangcentral.yangkit.model.api.schema;

import java.net.URI;
import java.util.Objects;
/**
 * the definition of module id information including name, revision,location,etc.
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class ModuleId {
   private String moduleName;
   private String revision;
   private URI location;

   public ModuleId(String moduleName, String revision) {
      this.moduleName = moduleName;
      this.revision = revision;
   }

   public String getModuleName() {
      return this.moduleName;
   }

   public String getRevision() {
      return this.revision;
   }

   public URI getLocation() {
      return this.location;
   }

   public void setLocation(URI location) {
      this.location = location;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ModuleId)) {
         return false;
      } else {
         ModuleId moduleId = (ModuleId)o;
         return this.getModuleName().equals(moduleId.getModuleName()) && Objects.equals(this.getRevision(), moduleId.getRevision());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getModuleName(), this.getRevision()});
   }
}
