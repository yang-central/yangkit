package org.yangcentral.yangkit.base;
/**
 * Position class, it can be used to edit/get position information
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class Position {
   private String fileName;
   private Location<?> location;

   public Position(String fileName, Location<?> location) {
      this.fileName = fileName;
      this.location = location;
   }

   public String getFileName() {
      return this.fileName;
   }

   public Location<?> getLocation() {
      return this.location;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("file:");
      sb.append(this.fileName);
      sb.append(" ");
      sb.append(this.location.getLocation());
      return sb.toString();
   }
}
