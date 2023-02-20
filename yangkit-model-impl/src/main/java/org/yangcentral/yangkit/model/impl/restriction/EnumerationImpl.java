package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Enumeration;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EnumerationImpl extends RestrictionImpl<String> implements Enumeration {
   private List<YangEnum> enums = new ArrayList<>();

   public EnumerationImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public EnumerationImpl(YangContext context) {
      super(context);
   }

   public Integer getHighestValue() {
      Typedef derived = this.getDerived();
      return derived != null ? null : this.getHighestValueFrom(this.enums.size());
   }

   public Integer getHighestValueFrom(int index) {
      Typedef derived = this.getDerived();
      if (derived != null) {
         return null;
      } else {
         if (index > this.enums.size()) {
            index = this.enums.size();
         }

         Integer highest = null;

         for(int i = 0; i < index; ++i) {
            YangEnum yEnum = (YangEnum)this.enums.get(i);
            Integer cur = null;
            if (yEnum.getValue() != null) {
               cur = yEnum.getValue().getValue();
            } else {
               if (highest == null) {
                  cur = 0;
               } else {
                  cur = highest + 1;
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

   private Integer getEnumActualValue(int index) {
      YangEnum yangEnum = (YangEnum)this.enums.get(index);
      if (yangEnum.getValue() != null) {
         return yangEnum.getValue().getValue();
      } else if (index == 0) {
         return 0;
      } else {
         Integer highest = this.getHighestValueFrom(index);
         return highest + 1;
      }
   }

   public Integer getEnumActualValue(String enumName) {
      Typedef derived = this.getDerived();
      if (derived != null) {
         Type builtinType = derived.getType().getBuiltinType();
         Enumeration builtinEnumeration = (Enumeration)builtinType.getRestriction();
         return builtinEnumeration.getEnumActualValue(enumName);
      } else {
         int pos = -1;

         for(int i = 0; i < this.enums.size(); ++i) {
            YangEnum yangEnum = (YangEnum)this.enums.get(i);
            if (yangEnum.getArgStr().equals(enumName)) {
               pos = i;
               break;
            }
         }

         return this.getEnumActualValue(pos);
      }
   }

   public List<YangEnum> getEnums() {
      return Collections.unmodifiableList(this.enums);
   }

   public boolean addEnum(YangEnum yangEnum) {
      Iterator<YangEnum> enumIterator = this.enums.iterator();

      YangEnum originalEnum;
      do {
         if (!enumIterator.hasNext()) {
            return this.enums.add(yangEnum);
         }

         originalEnum = enumIterator.next();
      } while(!originalEnum.getArgStr().equals(yangEnum.getArgStr()));

      return false;
   }

   public boolean evaluated(String value) {
      if (this.enums.size() > 0) {
         Iterator enumIterator = this.enums.iterator();

         while(enumIterator.hasNext()) {
            YangEnum yangEnum = (YangEnum)enumIterator.next();
            if (value.equals(yangEnum.getArgStr())) {
               return true;
            }
         }
      }

      return this.getDerived() != null ? this.getDerived().getType().getRestriction().evaluated(value) : false;
   }

   public List<YangEnum> getEffectiveEnums() {
      if (this.enums.size() > 0) {
         return this.enums;
      } else if (this.getDerived() != null) {
         Enumeration derivedEnumeration = (Enumeration)this.getDerived().getType().getRestriction();
         return derivedEnumeration.getEffectiveEnums();
      } else {
         return new ArrayList<>();
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Enumeration)) {
         return false;
      } else {
         EnumerationImpl anotherEnmeration = (EnumerationImpl)obj;
         List<YangEnum> thisEnums = this.getEffectiveEnums();
         List<YangEnum> anotherEnums = anotherEnmeration.getEffectiveEnums();
         if (thisEnums.size() != anotherEnums.size()) {
            return false;
         } else {
            Iterator enumIterator = thisEnums.iterator();

            YangEnum thisEnum;
            YangEnum anotherSame;
            do {
               if (!enumIterator.hasNext()) {
                  return true;
               }

               thisEnum = (YangEnum)enumIterator.next();
               anotherSame = null;
               Iterator antoherEnumIterator = anotherEnums.iterator();

               while(antoherEnumIterator.hasNext()) {
                  YangEnum anotherEnum = (YangEnum)antoherEnumIterator.next();
                  if (thisEnum.getArgStr().equals(anotherEnum.getArgStr())) {
                     anotherSame = anotherEnum;
                     break;
                  }
               }

               if (anotherSame == null) {
                  return false;
               }
            } while(this.getEnumActualValue(thisEnum.getArgStr()) == anotherEnmeration.getEnumActualValue(anotherSame.getArgStr()));

            return false;
         }
      }
   }
}
