package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UniqueImpl extends YangBuiltInStatementImpl implements Unique {
   private List<Leaf> uniqueNodes = new ArrayList();

   public UniqueImpl(String argStr) {
      super(argStr);
   }

   public List<Leaf> getUniqueNodes() {
      return Collections.unmodifiableList(this.uniqueNodes);
   }

   public boolean addUniqueNode(Leaf uniqueNode) {
      Iterator iterator = this.uniqueNodes.iterator();

      Leaf uni;
      do {
         if (!iterator.hasNext()) {
            return this.uniqueNodes.add(uniqueNode);
         }

         uni = (Leaf)iterator.next();
      } while(!uni.getArgStr().equals(uniqueNode.getArgStr()));

      return false;
   }

   @Override
   public void removeUniqueNodes() {
      this.uniqueNodes.clear();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.UNIQUE.getQName();
   }
}
