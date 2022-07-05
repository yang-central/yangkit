package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public interface TypedData<S extends TypedDataNode, T> extends YangData<S> {
   T getValue();

   String getStringValue();
}
