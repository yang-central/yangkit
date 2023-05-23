package org.yangcentral.yangkit.data.codec.json;

import com.google.gson.JsonElement;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

public class YangDataJsonCodec<S extends SchemaNode,T extends YangData<S>> implements YangDataCodec<S,T, JsonElement> {
    @Override
    public S getSchemaNode() {
        return null;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return null;
    }

    @Override
    public T deserialize(JsonElement element, ValidatorResultBuilder validatorResultBuilder) {
        return null;
    }

    @Override
    public JsonElement serialize(YangData<?> yangData) {
        return null;
    }
}
