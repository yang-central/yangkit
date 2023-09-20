package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataMessageCodec;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataMessage;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public abstract class YangDataMessageJsonCodec<M extends YangDataMessage<M>> implements YangDataMessageCodec<JsonNode,M> {
   private YangSchemaContext schemaContext;

    public YangDataMessageJsonCodec(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    protected abstract M parseHeader(JsonNode document, ValidatorResultBuilder builder);

    protected abstract YangDataContainer parseContent(JsonNode document,ValidatorResultBuilder builder);

    @Override
    public M deserialize(JsonNode document, ValidatorResultBuilder builder) {
        M m = parseHeader(document,builder);
        YangDataContainer content = parseContent(document,builder);
        try {
            m.getDocument().addChild((YangData<?>) content);
        } catch (YangDataException e) {
            throw new RuntimeException(e);
        }
        return m;
    }

    @Override
    public JsonNode serialize(M yangDataMessage) {
        return new YangDataDocumentJsonCodec(schemaContext).serialize(yangDataMessage.getDocument());
    }
}
