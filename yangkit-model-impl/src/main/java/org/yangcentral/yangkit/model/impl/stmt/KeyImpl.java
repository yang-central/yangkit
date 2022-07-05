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
   private List<Leaf> keyNodes = new ArrayList();

   public KeyImpl(String argStr) {
      super(argStr);
   }

   public List<Leaf> getkeyNodes() {
      return Collections.unmodifiableList(this.keyNodes);
   }

   public boolean addKeyNode(Leaf keyNode) {
      Iterator var2 = this.keyNodes.iterator();

      Leaf key;
      do {
         if (!var2.hasNext()) {
            return this.keyNodes.add(keyNode);
         }

         key = (Leaf)var2.next();
      } while(!key.getArgStr().equals(keyNode.getArgStr()));

      return false;
   }

   public Leaf getKeyNode(QName identifier) {
      Iterator var2 = this.keyNodes.iterator();

      Leaf key;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         key = (Leaf)var2.next();
      } while(!key.getIdentifier().equals(identifier));

      return key;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.KEY.getQName();
   }
}
