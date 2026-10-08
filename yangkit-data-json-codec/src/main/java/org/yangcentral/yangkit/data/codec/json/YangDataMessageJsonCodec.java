package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.YangDataMessageCodec;
import org.yangcentral.yangkit.data.api.model.YangDataMessage;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public abstract class YangDataMessageJsonCodec<M extends YangDataMessage<M>> implements YangDataMessageCodec<JsonNode,M> {
    private YangSchemaContext schemaContext;
    private AnydataValidationContextResolver anydataValidationContextResolver;

    public YangDataMessageJsonCodec(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    protected AnydataValidationContextResolver getAnydataValidationContextResolver() {
        return anydataValidationContextResolver;
    }

    protected abstract M parseHeader(JsonNode document, ValidatorResultBuilder builder);

    protected abstract void parseContent(JsonNode document,M m,ValidatorResultBuilder builder);

    @Override
    public M deserialize(JsonNode document, ValidatorResultBuilder builder) {
        return deserialize(document, builder, null);
    }

    public M deserialize(
            JsonNode document,
            ValidatorResultBuilder builder,
            AnydataValidationContextResolver resolver) {
        anydataValidationContextResolver = resolver;
        M m = parseHeader(document,builder);
        if (m != null) {
            parseContent(document, m, builder);
        }
        return m;
    }

    @Override
    public JsonNode serialize(M yangDataMessage) {
        return new YangDataDocumentJsonCodec(schemaContext).serialize(yangDataMessage.getBody());
    }
}
