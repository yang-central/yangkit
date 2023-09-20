package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public interface TypedData<D,S extends TypedDataNode> extends YangData<S> {
   YangDataValue<D,?> getValue();

   String getStringValue();
   String getStringValue(StringValueCodec<D> codec) throws YangCodecException;
}
