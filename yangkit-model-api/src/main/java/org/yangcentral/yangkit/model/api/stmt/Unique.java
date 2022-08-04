package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Unique extends YangBuiltinStatement {
   List<Leaf> getUniqueNodes();

   boolean addUniqueNode(Leaf uniqueNode);

   void removeUniqueNodes();

}
