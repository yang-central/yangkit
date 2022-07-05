package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Bits;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Bit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BitsImpl extends RestrictionImpl<List<String>> implements Bits {
   private List<Bit> bits = new ArrayList();

   public BitsImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public BitsImpl(YangContext context) {
      super(context);
   }

   public List<Bit> getBits() {
      return Collections.unmodifiableList(this.bits);
   }

   public Long getMaxPosition() {
      Typedef derived = this.getDerived();
      return derived != null ? null : this.getMaxPositionFrom(this.bits.size());
   }

   public Long getMaxPositionFrom(int index) {
      Typedef derived = this.getDerived();
      if (derived != null) {
         return null;
      } else {
         if (index > this.bits.size()) {
            index = this.bits.size();
         }

         Long highest = null;

         for(int i = 0; i < index; ++i) {
            Bit bit = (Bit)this.bits.get(i);
            Long cur = null;
            if (bit.getPosition() != null) {
               cur = bit.getPosition().getValue();
            } else {
               if (highest == null) {
                  cur = 0L;
               } else {
                  cur = highest + 1L;
               }

               highest = cur;
            }

            if (null == highest) {
               highest = cur;
            } else if (highest < cur) {
               highest = cur;
            }
         }

         return highest;
      }
   }

   private Long getBitActualPosition(int index) {
      Bit bit = (Bit)this.bits.get(index);
      if (bit.getPosition() != null) {
         return bit.getPosition().getValue();
      } else if (index == 0) {
         return 0L;
      } else {
         Long highest = this.getMaxPositionFrom(index);
         return highest + 1L;
      }
   }

   public Long getBitActualPosition(String bitName) {
      if (this.getDerived() != null) {
         Type base = this.getDerived().getType().getBuiltinType();
         BitsImpl bits = (BitsImpl)base.getRestriction();
         return bits.getBitActualPosition(bitName);
      } else {
         int pos = -1;

         for(int i = 0; i < this.bits.size(); ++i) {
            Bit bit = (Bit)this.bits.get(i);
            if (bit.getArgStr().equals(bitName)) {
               pos = i;
               break;
            }
         }

         return this.getBitActualPosition(pos);
      }
   }

   public boolean addBit(Bit bit) {
      Iterator var2 = this.bits.iterator();

      Bit orignalBit;
      do {
         if (!var2.hasNext()) {
            return this.bits.add(bit);
         }

         orignalBit = (Bit)var2.next();
      } while(!orignalBit.getArgStr().equals(bit.getArgStr()));

      return false;
   }

   public Bit getBit(String name) {
      Iterator var2 = this.bits.iterator();

      Bit bit;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         bit = (Bit)var2.next();
      } while(!name.equals(bit.getArgStr()));

      return bit;
   }

   public boolean evaluated(List<String> strings) {
      if (null != strings && strings.size() != 0) {
         if (this.bits.size() > 0) {
            Iterator var2 = strings.iterator();

            Bit bit;
            do {
               if (!var2.hasNext()) {
                  return true;
               }

               String str = (String)var2.next();
               bit = this.getBit(str);
            } while(bit != null);

            return false;
         } else {
            return this.getDerived() != null ? this.getDerived().getType().getRestriction().evaluated(strings) : false;
         }
      } else {
         return false;
      }
   }

   public List<Bit> getEffectiveBits() {
      if (this.bits.size() > 0) {
         return this.bits;
      } else if (this.getDerived() != null) {
         Bits derivedBits = (Bits)this.getDerived().getType().getRestriction();
         return derivedBits.getEffectiveBits();
      } else {
         return new ArrayList();
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Bits)) {
         return false;
      } else {
         BitsImpl another = (BitsImpl)obj;
         List<Bit> anotherBits = another.getEffectiveBits();
         List<Bit> thisBits = this.getEffectiveBits();
         if (thisBits.size() != anotherBits.size()) {
            return false;
         } else {
            for(int i = 0; i < thisBits.size(); ++i) {
               Bit bit = (Bit)thisBits.get(i);
               Bit anotherMatchedBit = null;

               for(int j = 0; j < anotherBits.size(); ++j) {
                  Bit anotherBit = (Bit)anotherBits.get(j);
                  if (bit.getArgStr().equals(anotherBit.getArgStr())) {
                     anotherMatchedBit = anotherBit;
                     break;
                  }
               }

               if (anotherMatchedBit == null || another.getBitActualPosition(anotherMatchedBit.getArgStr()) != this.getBitActualPosition(bit.getArgStr())) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
