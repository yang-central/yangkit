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
import org.yangcentral.yangkit.model.api.restriction.*;
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
        }else if(element.toString().equals("[null]")){
            throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                    "empty type keyword used when expected another type");
        }
        if(typedDataNode.getType().getRestriction() instanceof YangInteger || typedDataNode.getType().getRestriction() instanceof Decimal64){
            boolean mustBeJsonString =  typedDataNode.getType().getRestriction() instanceof Decimal64
                                        || typedDataNode.getType().getRestriction() instanceof Int64
                                        || typedDataNode.getType().getRestriction() instanceof UInt64;

            if(mustBeJsonString){
                if(element.isNumber()){
                    throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                            "A value of the 'int64', 'uint64', or 'decimal64' type is represented as a JSON string");
                }
            }else if(element.isTextual()){
                throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                        "A value of the 'int8', 'int16', 'int32', 'uint8', 'uint16', or 'uint32' type is represented as a JSON number.");
            }
        }
        if(typedDataNode.getType().getRestriction() instanceof YangBoolean){
            if(!element.isBoolean()){
                throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                        "A 'boolean' value is represented as the corresponding JSON literal name 'true' or 'false'");
            }
        }
        if(typedDataNode.getType().getRestriction() instanceof YangString){
            if(!element.isTextual()){
                throw new YangDataJsonCodecException(JsonCodecUtil.getJsonPath(element),element, ErrorTag.BAD_ELEMENT,
                        "A 'string' value is represented as a JSON string, subject to JSON string encoding rules");
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
