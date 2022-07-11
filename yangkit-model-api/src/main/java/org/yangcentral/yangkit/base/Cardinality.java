package org.yangcentral.yangkit.base;

/**
 * define cardinality, It will be used in YangStatementDef
 * @see org.yangcentral.yangkit.base.YangStatementDef
 * @author frank feng
 */
public class Cardinality {
   private boolean unbounded = true;
   private int minElements = 0;
   private int maxElements;

   public Cardinality() {
   }

/**
 * constructor,minElements need be specified, and maxElements default no limitation
 * @param minElements minimum value
 * @version 1.0.0
 * @throws
 * @return
 * @author frank feng
 * @since 7/7/2022
 */
   public Cardinality(int minElements) {
      this.minElements = minElements;
   }

   public Cardinality(int minElements, int maxElements) {
      this.minElements = minElements;
      this.maxElements = maxElements;
      this.unbounded = false;
   }

   /**
    *
    * @return
    */
   public boolean isUnbounded() {
      return this.unbounded;
   }

   public int getMinElements() {
      return this.minElements;
   }

   public int getMaxElements() {
      return this.maxElements;
   }
/**
 * check whether specified instances is valid
 * @param instances current instances
 * @version 1.0.0
 * @throws
 * @return boolean true is valid, false is invalid
 * @author frank feng
 * @since 7/7/2022
 */
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
