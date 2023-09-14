package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

abstract class TypedDataJsonCodec<S extends TypedDataNode, D extends TypedData<S>> extends YangDataJsonCodec<S, D> {
    protected TypedDataJsonCodec(S schemaNode) {
        super(schemaNode);
    }

    protected String getYangText(JsonNode element) throws YangDataJsonCodecException {
        String text = element.asText();
        return text;
    }

    @Override
    protected void buildElement(JsonNode element, YangData<?> yangData) {
    }

}
