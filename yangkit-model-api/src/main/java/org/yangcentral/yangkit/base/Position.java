package org.yangcentral.yangkit.base;
/**
 * Position class, it can be used to edit/get position information
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class Position {
   private String source;
   private Location<?> location;

   public Position(String source, Location<?> location) {
      this.source = source;
      this.location = location;
   }

   public String getSource() {
      return this.source;
   }

   public Location<?> getLocation() {
      return this.location;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("source:");
      sb.append(this.source);
      sb.append(" ");
      sb.append(this.location.getLocation());
      return sb.toString();
   }
}
