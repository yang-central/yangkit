package org.yangcentral.yangkit.model.api.codec;

import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public interface ComplexValueCodec<D, T> extends ValueCodec<D, T> {
   TypedDataNode getSchemaNode();
}
