package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public interface TypedData<S extends TypedDataNode> extends YangData<S> {
   YangDataValue<?,?> getValue();

   String getStringValue();
}
