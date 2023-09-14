package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

public class LeafDataJsonCodec extends TypedDataJsonCodec<Leaf, LeafData> {
    protected LeafDataJsonCodec(Leaf schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafData leafData = (LeafData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), yangText);
            return leafData;
        } catch (YangDataJsonCodecException e) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(e.getErrorPath());
            recordBuilder.setBadElement(e.getBadElement());
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
        }
        return null;
    }
}