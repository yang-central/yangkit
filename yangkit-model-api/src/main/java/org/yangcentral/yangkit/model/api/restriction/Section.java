package org.yangcentral.yangkit.model.api.restriction;

import java.math.BigDecimal;
import java.math.BigInteger;
/**
 * The definition of section, a section contains a min value and max value. For example, the 1..10 is one section, and
   1..max is another section
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class Section {
   private Comparable min;
   private Comparable max;

   public Section(Comparable max, Comparable min) {
      if (max.compareTo(min) < 0) {
         throw new IllegalArgumentException("max value:" + max + " is lower than min value:" + min);
      } else {
         this.max = max;
         this.min = min;
      }
   }

   public Comparable getMin() {
      return this.min;
   }

   public Comparable getMax() {
      return this.max;
   }
   /**
    * evaluate whether the value matches the section.
    * @param val value
    * @version 1.0.0
    * @return boolean true: the value matches the section(greater than or equals the min and less than or equals the max)
    *                 false: the value doesn't match the section.
    * @author frank feng
    * @since 7/8/2022
    */
   public boolean evaluate(Comparable val) {
      return val.compareTo(this.min) >= 0 && val.compareTo(this.max) <= 0;
   }
   /**
    * judge whether this section is the sub section of super section
    * @param superSection  super section
    * @version 1.0.0
    * @return boolean
    * @author frank feng
    * @since 7/8/2022
    */
   public boolean isSubSection(Section superSection) {
      Comparable thisMin = null;
      Comparable superMin = null;
      Comparable thisMax = null;
      Comparable superMax = null;
      if (!(this.min instanceof BigDecimal) && !(superSection.getMin() instanceof BigDecimal)) {
         thisMin = new BigInteger(this.min.toString());
         superMin = new BigInteger(superSection.getMin().toString());
         thisMax = new BigInteger(this.max.toString());
         superMax = new BigInteger(superSection.getMax().toString());
      } else {
         thisMin = new BigDecimal(this.min.toString());
         superMin = new BigDecimal(superSection.getMin().toString());
         thisMax = new BigDecimal(this.max.toString());
         superMax = new BigDecimal(superSection.getMax().toString());
      }

      return ((Comparable)thisMin).compareTo(superMin) >= 0 && ((Comparable)thisMax).compareTo(superMax) <= 0;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Section)) {
         return false;
      } else {
         Section another = (Section)obj;
         Comparable thisMin = null;
         Comparable anotherMin = null;
         Comparable thisMax = null;
         Comparable anotherMax = null;
         if (!(this.min instanceof BigDecimal) && !(another.getMin() instanceof BigDecimal)) {
            thisMin = new BigInteger(this.min.toString());
            anotherMin = new BigInteger(another.getMin().toString());
            thisMax = new BigInteger(this.max.toString());
            anotherMax = new BigInteger(another.getMax().toString());
         } else {
            thisMin = new BigDecimal(this.min.toString());
            anotherMin = new BigDecimal(another.getMin().toString());
            thisMax = new BigDecimal(this.max.toString());
            anotherMax = new BigDecimal(another.getMax().toString());
         }
         return (thisMin.compareTo(anotherMin) == 0) && (thisMax.compareTo(anotherMax) ==0);
      }
   }
}
