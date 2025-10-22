package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public class LeafListDataJsonCodec extends TypedDataJsonCodec<LeafList, LeafListData<?>> {
    protected LeafListDataJsonCodec(LeafList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafListData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafListData leafListData = (LeafListData) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(), yangText);
            TypedDataNode typedData = (TypedDataNode) leafListData.getSchemaNode();
            if (typedData.getType().getRestriction() instanceof IdentityRef){
                leafListData.getStringValue(new IdentityRefJsonCodec(typedData));
            } else {
                leafListData.getStringValue();
            }
            return leafListData;
        } catch (YangDataJsonCodecException e) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(e.getErrorPath());
            recordBuilder.setBadElement(e.getBadElement());
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
        } catch(NullPointerException ignored){
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(element));
            validatorResultBuilder.addRecord(recordBuilder.build());
        } catch (YangCodecException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}