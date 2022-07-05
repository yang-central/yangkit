package org.yangcentral.yangkit.base;

public class Cardinality {
   private boolean unbounded = true;
   private int minElements = 0;
   private int maxElements;

   public Cardinality() {
   }

   public Cardinality(int minElements) {
      this.minElements = minElements;
   }

   public Cardinality(int minElements, int maxElements) {
      this.minElements = minElements;
      this.maxElements = maxElements;
      this.unbounded = false;
   }

   public boolean isUnbounded() {
      return this.unbounded;
   }

   public int getMinElements() {
      return this.minElements;
   }

   public int getMaxElements() {
      return this.maxElements;
   }

   public boolean isValid(int instances) {
      if (instances < this.minElements) {
         return false;
      } else if (this.isUnbounded()) {
         return true;
      } else {
         return instances <= this.maxElements;
      }
   }

   public String toString() {
      if (this.unbounded) {
         return this.minElements + " .. n";
      } else {
         return this.minElements == this.maxElements ? Integer.toString(this.maxElements) : this.minElements + " .. " + this.maxElements;
      }
   }
}
