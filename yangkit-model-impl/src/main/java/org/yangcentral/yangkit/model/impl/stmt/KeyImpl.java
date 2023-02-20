package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;

import org.yangcentral.yangkit.model.api.stmt.Key;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class KeyImpl extends YangBuiltInStatementImpl implements Key {
   private List<Leaf> keyNodes = new ArrayList<>();

   public KeyImpl(String argStr) {
      super(argStr);
   }

   public List<Leaf> getkeyNodes() {
      return Collections.unmodifiableList(this.keyNodes);
   }

   public boolean addKeyNode(Leaf keyNode) {
      Iterator<Leaf> leafIterator = this.keyNodes.iterator();

      Leaf key;
      do {
         if (!leafIterator.hasNext()) {
            return this.keyNodes.add(keyNode);
         }

         key = leafIterator.next();
      } while(!key.getArgStr().equals(keyNode.getArgStr()));

      return false;
   }



   public Leaf getKeyNode(QName identifier) {
      Iterator<Leaf> leafIterator = this.keyNodes.iterator();

      Leaf key;
      do {
         if (!leafIterator.hasNext()) {
            return null;
         }

         key = leafIterator.next();
      } while(!key.getIdentifier().equals(identifier));

      return key;
   }

   @Override
   public Leaf removeKeyNode(QName identifier) {
      for(Leaf node:keyNodes){
         if(node.getIdentifier().equals(identifier)){
            keyNodes.remove(node);
            return node;
         }
      }
      return null;
   }

   @Override
   public void removeKeyNodes() {
      this.keyNodes.clear();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.KEY.getQName();
   }

}
