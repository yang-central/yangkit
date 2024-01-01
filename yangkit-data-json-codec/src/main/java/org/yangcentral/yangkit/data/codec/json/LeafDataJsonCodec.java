package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

public class LeafDataJsonCodec extends TypedDataJsonCodec<Leaf, LeafData<?>> {
    protected LeafDataJsonCodec(Leaf schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafData leafData = null;
            if(getSchemaNode().getType().getRestriction() instanceof IdentityRef){
                leafData = (LeafData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), yangText,
                        new IdentityRefJsonCodec(getSchemaNode()));
            } else {
                leafData = (LeafData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), yangText);
                leafData.getValue().getStringValue(); //TODO: vivek trigger error
            }
            return leafData;
        } catch (YangDataJsonCodecException e) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(e.getErrorPath());
            recordBuilder.setBadElement(e.getBadElement());
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
        } catch (YangCodecException e){
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(element));
            validatorResultBuilder.addRecord(recordBuilder.build());
        }
        return null;
    }
}