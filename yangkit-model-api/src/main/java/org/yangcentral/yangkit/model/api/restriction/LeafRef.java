package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;

public interface LeafRef extends Restriction<Object> {
   Path getPath();

   Path getEffectivePath();

   RequireInstance getRequireInstance();

   RequireInstance getEffectiveRequireInstance();

   boolean isRequireInstance();

   TypedDataNode getReferencedNode();

   void setReferencedNode(TypedDataNode var1);
}
