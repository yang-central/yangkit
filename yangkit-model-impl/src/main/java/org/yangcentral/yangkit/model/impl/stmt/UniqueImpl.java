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
      Iterator var2 = this.uniqueNodes.iterator();

      Leaf uni;
      do {
         if (!var2.hasNext()) {
            return this.uniqueNodes.add(uniqueNode);
         }

         uni = (Leaf)var2.next();
      } while(!uni.getArgStr().equals(uniqueNode.getArgStr()));

      return false;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.UNIQUE.getQName();
   }
}
