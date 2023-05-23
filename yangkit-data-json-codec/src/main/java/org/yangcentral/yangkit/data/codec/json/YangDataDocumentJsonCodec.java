package org.yangcentral.yangkit.data.codec.json;

import com.google.gson.JsonElement;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataDocumentCodec;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public class YangDataDocumentJsonCodec implements YangDataDocumentCodec<JsonElement> {
    @Override
    public YangSchemaContext getSchemaContext() {
        return null;
    }

    @Override
    public YangDataDocument deserialize(JsonElement var1, ValidatorResultBuilder var2) {
        return null;
    }

    @Override
    public JsonElement serialize(YangDataDocument var1) {
        return null;
    }
}
