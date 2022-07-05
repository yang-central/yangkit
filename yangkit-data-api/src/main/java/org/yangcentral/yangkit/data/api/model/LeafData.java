package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Leaf;

public interface LeafData<T> extends TypedData<Leaf, T> {
   void setValue(T var1, String var2);
}
