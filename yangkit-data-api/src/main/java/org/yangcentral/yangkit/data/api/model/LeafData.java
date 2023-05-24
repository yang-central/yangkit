package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Leaf;

public interface LeafData extends TypedData<Leaf> {

    void setValue(YangDataValue<?,?> value);
}
