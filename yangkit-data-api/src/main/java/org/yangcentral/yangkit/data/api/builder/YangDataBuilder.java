package org.yangcentral.yangkit.data.api.builder;

import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.ValueCodec;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

public interface YangDataBuilder {
    YangData<?> getYangData(SchemaNode schemaNode, Object value);
    YangData<?> getYangData(SchemaNode schemaNode, Object value, ValueCodec<?,?> codec);
}
