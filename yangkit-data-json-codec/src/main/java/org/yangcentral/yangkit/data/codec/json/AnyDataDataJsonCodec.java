package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

public class AnyDataDataJsonCodec extends YangDataJsonCodec<Anydata, AnyDataData> {

    protected AnyDataDataJsonCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyDataData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(getSchemaContext());
        YangDataDocument dataDocument = documentJsonCodec.deserialize(element, validatorResultBuilder);
        YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), dataDocument);
        return null;
    }

    @Override
    protected JsonNode buildElement(YangData<?> yangData) {
        AnyDataData anyDataData = (AnyDataData) yangData;
        YangDataDocument document = anyDataData.getValue();
        YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(getSchemaContext());
        JsonNode root = documentJsonCodec.serialize(document);
        return root;
    }
}