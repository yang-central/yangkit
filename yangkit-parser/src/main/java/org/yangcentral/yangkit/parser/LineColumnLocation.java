package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.Location;

import java.util.Objects;

public class LineColumnLocation implements Location<String> {
   private int line;
   private int column;

   public LineColumnLocation(int line, int column) {
      this.line = line;
      this.column = column;
   }

   public int getLine() {
      return this.line;
   }

   public int getColumn() {
      return this.column;
   }

   public String getLocation() {
      StringBuffer sb = new StringBuffer(" line:");
      sb.append(this.line);
      sb.append(" column:");
      sb.append(this.column);
      return sb.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LineColumnLocation that = (LineColumnLocation) o;
      return line == that.line && column == that.column;
   }

   @Override
   public int hashCode() {
      return Objects.hash(line, column);
   }
}
