package org.yangcentral.yangkit.model.impl.restriction;

import org.apache.commons.lang3.ObjectUtils;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UnionImpl extends RestrictionImpl<Object> implements Union {
   private List<Type> types = new ArrayList<>();

   public UnionImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public UnionImpl(YangContext context) {
      super(context);
   }

   public boolean evaluate(Object value) {
      for(Type type: types){
         Restriction restriction = type.getRestriction();
         if(restriction instanceof Binary){
            if(!(value instanceof byte[])){
               continue;
            }
         } else if (restriction instanceof Bits){
            if(!(value instanceof List)){
               continue;
            }
         } else if (restriction instanceof Decimal64) {
            if(!(value instanceof BigDecimal)){
               continue;
            }
         } else if (restriction instanceof Empty){
            if(!(value instanceof ObjectUtils.Null)){
               continue;
            }
         } else if (restriction instanceof Enumeration){
            if(!(value instanceof String)){
               continue;
            }
         } else if (restriction instanceof IdentityRef){
            if(!(value instanceof QName)){
               continue;
            }
         } else if (restriction instanceof InstanceIdentifier){
            if(!(value instanceof YangAbsoluteLocationPath)){
               continue;
            }
         } else if (restriction instanceof Int16){
            if(!(value instanceof Short)){
               continue;
            }
         } else if (restriction instanceof Int32){
            if(!(value instanceof Integer)){
               continue;
            }
         } else if (restriction instanceof Int64){
            if(!(value instanceof Long)){
               continue;
            }
         }  else if (restriction instanceof Int8){
            if(!(value instanceof Byte)){
               continue;
            }
         }  else if (restriction instanceof LeafRef){

         }  else if (restriction instanceof UInt16){
            if(!(value instanceof Integer)){
               continue;
            }
         }  else if (restriction instanceof UInt32){
            if(!(value instanceof Long)){
               continue;
            }
         } else if (restriction instanceof UInt64){
            if(!(value instanceof BigInteger)){
               continue;
            }
         } else if (restriction instanceof UInt8){
            if(!(value instanceof Short)){
               continue;
            }
         } else if (restriction instanceof YangBoolean){
            if(!(value instanceof Boolean)){
               continue;
            }
         } else if (restriction instanceof YangString){
            if(!(value instanceof String)){
               continue;
            }
         }
         if(restriction.evaluate(value)){
            return true;
         }

      }
      if(getDerived()!= null){
         return getDerived().getType().getRestriction().evaluate(value);
      }
      return false;
   }

   public List<Type> getTypes() {
      return Collections.unmodifiableList(this.types);
   }

   public boolean addType(Type type) {
      return this.types.add(type);
   }

   public List<Type> getActualTypes() {
      if (this.types.size() > 0) {
         return this.types;
      } else if (this.getDerived() != null) {
         UnionImpl derived = (UnionImpl)this.getDerived().getType().getRestriction();
         return derived.getActualTypes();
      } else {
         return new ArrayList<>();
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Union)) {
         return false;
      } else {
         UnionImpl another = (UnionImpl)obj;
         List<Type> thisTypes = this.getActualTypes();
         List<Type> anotherTypes = another.getActualTypes();
         if (thisTypes.size() != anotherTypes.size()) {
            return false;
         } else {
            Iterator typeIterator = thisTypes.iterator();

            Type theSame;
            do {
               if (!typeIterator.hasNext()) {
                  return true;
               }

               Type thisType = (Type)typeIterator.next();
               theSame = null;
               Iterator antoherTypeIterator = anotherTypes.iterator();

               while(antoherTypeIterator.hasNext()) {
                  Type anotherType = (Type)antoherTypeIterator.next();
                  if (thisType.getRestriction().equals(anotherType.getRestriction())) {
                     theSame = anotherType;
                     break;
                  }
               }
            } while(theSame != null);

            return false;
         }
      }
   }
}
