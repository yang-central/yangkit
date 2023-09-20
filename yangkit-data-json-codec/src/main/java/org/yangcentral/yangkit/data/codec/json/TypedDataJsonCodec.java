package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Empty;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

abstract class TypedDataJsonCodec<S extends TypedDataNode, D extends TypedData<?,S>> extends YangDataJsonCodec<S, D> {
    protected TypedDataJsonCodec(S schemaNode) {
        super(schemaNode);
    }

    protected String getYangText(JsonNode element) throws YangDataJsonCodecException {
        TypedDataNode typedDataNode = getSchemaNode();
        if(typedDataNode.getType().getRestriction() instanceof Empty){
            if(element.isArray()){
                ArrayNode arrayNode = (ArrayNode) element;
                int size = arrayNode.size();
                if(size != 1){
                    throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                            "wrong format for empty type, it should be [null]");
                }
                JsonNode nullNode = arrayNode.get(0);
                if(!nullNode.isNull()){
                    throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                            "wrong format for empty type, it should be [null]");
                }
                return "";
            } else {
                throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                        "wrong format for empty type, it should be [null]");
            }
        }
        String text = element.asText();
        return text;
    }

    @Override
    protected JsonNode buildElement(YangData<?> yangData) {

        TypedData typedData = (TypedData) yangData;
        return TextNode.valueOf(typedData.getStringValue());
    }

}
