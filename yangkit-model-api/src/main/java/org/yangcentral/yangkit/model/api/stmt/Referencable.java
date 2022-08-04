package org.yangcentral.yangkit.model.api.stmt;

import java.util.Iterator;
import java.util.List;

public interface Referencable {
   List<YangStatement> getReferencedBy();

   default void addReference(YangStatement yangStatement){
      for(YangStatement statement:getReferencedBy()){
         if(statement == yangStatement){
            return;
         }
      }
      getReferencedBy().add(yangStatement);
   }

   default void delReference(YangStatement yangStatement){
      int pos = -1;

      for(int i = 0; i < this.getReferencedBy().size(); ++i) {
         if (this.getReferencedBy().get(i) == yangStatement) {
            pos = i;
            break;
         }
      }

      if (pos != -1) {
         this.getReferencedBy().remove(pos);
      }
   }

   default boolean isReferencedBy(YangStatement yangStatement){
      Iterator iterator = this.getReferencedBy().iterator();

      YangStatement statement;
      do {
         if (!iterator.hasNext()) {
            return false;
         }

         statement = (YangStatement)iterator.next();
      } while(statement != yangStatement);

      return true;
   }

   default boolean isReferenced(){
      if(getReferencedBy() == null){
         return false;
      }
      return !getReferencedBy().isEmpty();
   }
}
