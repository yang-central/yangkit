package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

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
         if(type.getRestriction().evaluate(value)){
            return true;
         }
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
