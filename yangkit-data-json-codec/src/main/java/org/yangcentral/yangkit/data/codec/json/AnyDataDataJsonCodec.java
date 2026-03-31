package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContext;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationRequest;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

public class AnyDataDataJsonCodec extends YangDataJsonCodec<Anydata, AnyDataData> {

    protected AnyDataDataJsonCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyDataData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        YangSchemaContext payloadSchemaContext = getSchemaContext();
        if (getAnydataValidationContextResolver() != null) {
            AnydataValidationContext context = getAnydataValidationContextResolver().resolve(
                    new AnydataValidationRequest(getSchemaNode(), getSourcePath(), getSchemaContext()));
            if (context != null && context.getSchemaContext() != null) {
                payloadSchemaContext = context.getSchemaContext();
            }
        }
        YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(payloadSchemaContext);
        YangDataDocument dataDocument = documentJsonCodec.deserialize(element, validatorResultBuilder,
                getAnydataValidationContextResolver());
        return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), dataDocument);
    }

    @Override
    protected JsonNode buildElement(YangData<?> yangData) {
        AnyDataData anyDataData = (AnyDataData) yangData;
        YangDataDocument document = anyDataData.getEffectiveValue();
        YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(getSchemaContext());
        JsonNode root = documentJsonCodec.serialize(document);
        return root;
    }
}