package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.QName;
import java.util.List;

public interface Key extends YangBuiltinStatement {
   List<Leaf> getkeyNodes();

   boolean addKeyNode(Leaf keyNode);

   Leaf getKeyNode(QName identifier);
}
