package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationSupport;
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
        if (!element.isObject()) {
            AnydataValidationSupport.recordInvalidContent(
                    getSchemaNode(), getSourcePath(), element,
                    "JSON content must be an object.", validatorResultBuilder);
            return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        }
        if (element.isEmpty()) {
            return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        }
        YangSchemaContext payloadSchemaContext = AnydataValidationSupport.resolveSchemaContext(
                getSchemaNode(), getSourcePath(), getSchemaContext(),
                getAnydataValidationContextResolver(), element, validatorResultBuilder);
        if (payloadSchemaContext == null) {
            return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
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
        if (document == null) {
            return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        }
        YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(document.getSchemaContext());
        JsonNode root = documentJsonCodec.serialize(document);
        return root;
    }
}