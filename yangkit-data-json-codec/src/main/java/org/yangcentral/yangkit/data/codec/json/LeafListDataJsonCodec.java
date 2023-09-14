package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

public class LeafListDataJsonCodec extends TypedDataJsonCodec<LeafList, LeafListData> {
    protected LeafListDataJsonCodec(LeafList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafListData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafListData leafListData = (LeafListData) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(), yangText);
            return leafListData;
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

    @Override
    protected void buildElement(JsonNode element, YangData<?> yangData) {
    }

}