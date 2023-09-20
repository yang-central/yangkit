package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Leaf;

public interface LeafData<D> extends TypedData<D,Leaf> {

    void setValue(YangDataValue<D,?> value);
}
