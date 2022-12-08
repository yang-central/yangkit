package org.yangcentral.yangkit.base;

import java.util.Objects;

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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Position position = (Position) o;
      return source.equals(position.source) && location.equals(position.location);
   }

   @Override
   public int hashCode() {
      return Objects.hash(source, location);
   }
}
