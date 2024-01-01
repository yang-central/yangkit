package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.*;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangDataEntity;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.model.api.restriction.Empty;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

public class JsonCodecUtil {
    public static String generateJsonPathArgumentFromJson(JsonNode jsonNode, String valueSearched) {
        if (jsonNode.isValueNode() && !jsonNode.asText().equals(valueSearched)) {
            return null;
        } else {
            if (jsonNode.isContainerNode()) {
                if (jsonNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> elements = jsonNode.fields();
                    while (elements.hasNext()) {
                        Map.Entry<String, JsonNode> element = elements.next();
                        String res =  generateJsonPathArgumentFromJson(element.getValue(), valueSearched);
                        if (res != null) {
                            return "." + element.getKey() + res;
                        }
                    }
                } else {
                    int i = 0;
                    Iterator<JsonNode> elements = jsonNode.elements();
                    while (elements.hasNext()) {
                        JsonNode element = elements.next();
                        String res = generateJsonPathArgumentFromJson(element, valueSearched);
                        if (res != null) {
                            return "(" + i + ")" + res;
                        }
                        i++;
                    }
                }
            }
        }
        return "";
    }

    public static Map<JsonNode, String> jsonPath = new HashMap<>();
    public static Map<JsonNode, JsonNode> jsonNodeParent = new HashMap<>();
    public static String getJsonPath(JsonNode jsonNode) {
        StringBuilder path = new StringBuilder();
        JsonNode parent = jsonNodeParent.get(jsonNode);
        while(parent != null){
            path.insert(0, jsonPath.get(jsonNode) + "/");
            jsonNode = parent;
            parent = jsonNodeParent.get(jsonNode);
        }
        path.insert(0, "/");
        path.deleteCharAt(path.length() - 1);
        return path.toString();
    }

    public static QName getQNameFromJsonField(String jsonField, YangDataContainer parent){
        FName fName = new FName(jsonField);
        String moduleName = fName.getPrefix();
        URI ns = null;
        if(moduleName == null) {
            if(parent instanceof YangDataDocument){
                YangDataDocument yangDataDocument = (YangDataDocument) parent;
                ns = yangDataDocument.getQName().getNamespace();
            } else {
                YangData<?> yangData = (YangData<?>) parent;
                ns = yangData.getQName().getNamespace();
            }
        } else {
            YangSchemaContext schemaContext = null;
            if(parent instanceof YangDataDocument){
                YangDataDocument yangDataDocument = (YangDataDocument) parent;
                schemaContext = yangDataDocument.getSchemaContext();
            } else {
                YangData<?> yangData = (YangData<?>) parent;
                schemaContext = yangData.getSchemaNode().getContext().getSchemaContext();
            }
            Optional<Module> moduleOp = schemaContext.getLatestModule(moduleName);
            if(!moduleOp.isPresent()){
                return null;
            }
            ns = moduleOp.get().getMainModule().getNamespace().getUri();
        }

        return new QName(ns,fName.getLocalName());
    }
    public static QName getQNameFromJsonField(String jsonField, YangSchemaContext schemaContext){
        FName fName = new FName(jsonField);
        String moduleName = fName.getPrefix();
        URI ns = null;
        if(moduleName == null) {
            return null;
        }
        if(moduleName.equals("ietf-restconf")){
            return new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","ietf-restconf",fName.getLocalName());
        }
        Optional<Module> moduleOp = schemaContext.getLatestModule(moduleName);
        if(!moduleOp.isPresent()){
            return null;
        }
        ns = moduleOp.get().getMainModule().getNamespace().getUri();

        return new QName(ns,fName.getLocalName());
    }

    public static String getJsonFieldFromQName(QName qName,YangSchemaContext schemaContext) {
        if(qName == null){
            return null;
        }
        if(qName.getNamespace().toString().equals("urn:ietf:params:xml:ns:yang:ietf-restconf")){
            return "ietf-restconf:"+ qName.getLocalName();
        }
        List<Module> modules = schemaContext.getModule(qName.getNamespace());
        if(modules.isEmpty()) {
            return qName.getQualifiedName();
        }
        String moduleName = modules.get(0).getMainModule().getArgStr();
        return moduleName + ":" + qName.getLocalName();
    }
    public static String getJsonFieldFromQName(QName qName,YangDataContainer parent) {
        if(qName == null){
            return null;
        }
        if(qName.getNamespace().toString().equals("urn:ietf:params:xml:ns:yang:ietf-restconf")){
            return "ietf-restconf:"+ qName.getLocalName();
        }
        QName parentQName = null;
        YangSchemaContext schemaContext = null;
        if(parent instanceof YangDataDocument){
            YangDataDocument yangDataDocument = (YangDataDocument) parent;
            parentQName = yangDataDocument.getQName();
            schemaContext = yangDataDocument.getSchemaContext();
        } else {
            YangData<?> yangData = (YangData<?>) parent;
            parentQName = yangData.getQName();
            schemaContext = yangData.getSchemaNode().getContext().getSchemaContext();
        }
        if(parentQName != null && parentQName.getNamespace().equals(qName.getNamespace())){
            return qName.getLocalName();
        }
        List<Module> modules = schemaContext.getModule(qName.getNamespace());
        if(modules.isEmpty()) {
            return qName.getQualifiedName();
        }
        String moduleName = modules.get(0).getMainModule().getArgStr();
        return moduleName + ":" + qName.getLocalName();
    }
    public static  void serializeChildren(ObjectNode element, YangDataContainer yangDataContainer) {
        ObjectMapper mapper = new ObjectMapper();
        List<YangData<?>> children = yangDataContainer.getDataChildren();
        if (null == children) {
            return;
        }
        for (YangData child : children) {
            if (null == child || child.isDummyNode()) {
                continue;
            }
            String fieldName = JsonCodecUtil.getJsonFieldFromQName(child.getQName(),yangDataContainer);
            JsonNode childElement = YangDataJsonCodec
                    .getInstance(child.getSchemaNode())
                    .serialize(child);
            if((child.getSchemaNode() instanceof YangList)
                    || (child.getSchemaNode() instanceof LeafList)) {
                ArrayNode arrayNode = (ArrayNode) element.get(fieldName);
                if(arrayNode == null) {
                    arrayNode = mapper.createArrayNode();
                    element.put(fieldName,arrayNode);
                }
                arrayNode.add(childElement);
            } else {
                element.put(fieldName, childElement);
            }
        }
    }

    public static void processAttribute(String key, JsonNode attributeValue, YangDataContainer yangDataContainer) throws YangDataJsonCodecException {
        List<Attribute> attributeList = new ArrayList<>();
        YangSchemaContext schemaContext = null;
        if(yangDataContainer instanceof YangDataDocument){
            schemaContext = ((YangDataDocument) yangDataContainer).getSchemaContext();
        } else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaContext = yangData.getSchemaNode().getContext().getSchemaContext();
        }
        AttributeBlock attributeBlock = new AttributeBlock(attributeValue,schemaContext);
        if (key.equals("@")) {
            //container/anydata/list
            attributeList = attributeBlock.getAttributes();
            if (attributeList.size() > 0) {
                for (Attribute attribute : attributeList) {
                    ((YangDataEntity) yangDataContainer).addAttribute(attribute);
                }
            }
        } else {
            String cKey = key.substring(1);
            QName qName = JsonCodecUtil.getQNameFromJsonField(cKey,yangDataContainer);
            List<YangData<?>> children = yangDataContainer.getDataChildren(qName);
            if( children.isEmpty()){
                return;
            }
            if(!attributeBlock.getChildren().isEmpty()){
                //leaf-list
                int size = attributeBlock.getChildren().size();
                if(size > children.size()){
                    throw new YangDataJsonCodecException(getJsonPath(attributeBlock.getJsonNode()),
                            attributeBlock.getJsonNode(),ErrorTag.BAD_ELEMENT,
                            "The size of annotations are greater than instances' size.");
                }
                for(int i =0; i< size;i++){
                    children.get(i).setAttributes(attributeBlock.getChildren().get(i).getAttributes());
                }
            } else {
                //anyxml or leaf-list
                children.get(0).setAttributes(attributeBlock.getAttributes());
            }

        }
    }

    public static ValidatorRecordBuilder<String, JsonNode> getTypeErrorRecord(JsonNode child, String expectedType){
        ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
        recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
        recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
        recordBuilder.setBadElement(child);
        recordBuilder.setErrorMessage(new ErrorMessage("bad element:" + child.toString() + " does not match type " + expectedType ));
        return recordBuilder;
    }

    public static ValidatorRecordBuilder<String, JsonNode> getRestrictionErrorRecord(JsonNode child, String restriction){
        ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
        recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
        recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
        recordBuilder.setBadElement(child);
        recordBuilder.setErrorMessage(new ErrorMessage("bad element:" + child.toString() + " does not respect restriction : " + restriction ));
        return recordBuilder;
    }

    public static ValidatorResult checkLeaf(JsonNode child, Leaf leaf){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        String builtinType = leaf.getType().getBuiltinType().getArgStr().toLowerCase();
        switch (builtinType){
            case "boolean":
                if(!child.isBoolean()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                }
                break;
            case "string":
            case "enumeration":
                if(!child.isTextual()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                }else if(!leaf.getType().getRestriction().evaluate(child.asText())){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;
            case "int8":
                byte convertedB;
                if(!child.isNumber()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedB = Byte.parseByte(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedB)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "int16":
                short convertedS;
                if(!child.isNumber()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedS = Short.parseShort(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedS)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "int32":
                int convertedI;
                if(!child.isNumber()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedI = Integer.parseInt(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedI)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "int64":
                long convertedL;
                if(!child.isTextual()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedL = Long.parseLong(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedL)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "uint8":
                short convertedUS;
                if(!child.isNumber() && !child.asText().startsWith("-")){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedUS = Short.parseShort(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedUS)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "uint16":
                int convertedUI;
                if(!child.isNumber()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedUI = Integer.parseUnsignedInt(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedUI)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;
            case "uint32":
                long convertedUL;
                if(!child.isNumber()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedUL = Long.parseUnsignedLong(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedUL)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "uint64":
                BigInteger convertedU64;
                if(!child.isTextual()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedU64 = new BigInteger(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedU64)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;

            case "decimal64":
                BigDecimal convertedDec;
                if(!child.isTextual()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                try{
                    convertedDec = new BigDecimal(child.asText());
                }catch (NumberFormatException e) {
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                    break;
                }
                if(!leaf.getType().getRestriction().evaluate(convertedDec)){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;
            case "binary":
                if(!child.isTextual()){
                    validatorResultBuilder.addRecord(getTypeErrorRecord(child, builtinType).build());
                }else if(!leaf.getType().getRestriction().evaluate(child.asText().getBytes())){
                    validatorResultBuilder.addRecord(getRestrictionErrorRecord(child, leaf.getType().getRestriction().toString()).build());
                }
                break;
        }
        return validatorResultBuilder.build();
    }

    public static ValidatorResult buildChildData(YangDataContainer yangDataContainer, JsonNode child, SchemaNode childSchemaNode){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

        boolean doArrayValidation = true;
        if(childSchemaNode instanceof Container){
            doArrayValidation = false;
        }
        else if(childSchemaNode instanceof Leaf){
            Leaf leaf = (Leaf)childSchemaNode;
            doArrayValidation = leaf.getType() instanceof Empty;
        }

        if(child.isArray() && doArrayValidation) {
            if((childSchemaNode instanceof YangList) || (childSchemaNode instanceof LeafList)) {
                int size = child.size();
                for (int i =0;i < size;i++) {
                    JsonNode childElement = child.get(i);
                    JsonCodecUtil.jsonNodeParent.put(childElement,child);
                    JsonCodecUtil.jsonPath.put(childElement, Integer.toString(i));
                    validatorResultBuilder.merge(buildChildData(yangDataContainer,childElement,childSchemaNode));
                }
            } else {
                ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
                recordBuilder.setBadElement(child);
                recordBuilder.setErrorMessage(new ErrorMessage(
                        "bad element:" +childSchemaNode.getJsonIdentifier()));
                validatorResultBuilder.addRecord(recordBuilder.build());
            }
            return validatorResultBuilder.build();
        }
        YangDataJsonCodec sonCodec = YangDataJsonCodec.getInstance(childSchemaNode);
        YangData<?> sonData = sonCodec.deserialize(child, validatorResultBuilder);
        if (null == sonData) {
            return validatorResultBuilder.build();
        }
        try {
            YangData<?> oldData = yangDataContainer.getDataChild(sonData.getIdentifier());
            if (oldData != null) {
                YangDataOperator dataOperator = new YangDataOperatorImpl(yangDataContainer);
                dataOperator.merge((YangData<? extends DataNode>) sonData, false);
            } else {
                yangDataContainer.addDataChild(sonData, false);
            }

        } catch (YangDataException e) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(child.toString());
            recordBuilder.setBadElement(child);
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
            return validatorResultBuilder.build();
        }
        sonData = yangDataContainer.getDataChild(sonData.getIdentifier());
        if (sonData instanceof YangDataContainer) {
            validatorResultBuilder.merge(buildChildrenData((YangDataContainer) sonData, child));
        }
        return validatorResultBuilder.build();
    }

    /**
     * build child data according to parent schema and json
     * @param schemaNodeContainer parent schema or schema context
     * @param child  json representation of child
     * @param validatorResultBuilder validator result builder
     * @return YangData representation
     */
    public static YangData<?> buildChildData(SchemaNodeContainer schemaNodeContainer,
                                             JsonNode child,
                                             ValidatorResultBuilder validatorResultBuilder){
        if (child.isNull()) {
            return null;
        }
        int size = 0;
        Iterator<String> fields = child.fieldNames();
        while (fields.hasNext()) {
            fields.next();
            size++;
        }
        if(size == 0) {
            return null;
        }
        if(size > 1) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
            recordBuilder.setBadElement(child);
            recordBuilder.setErrorMessage(new ErrorMessage("wrong format, only one root node can be allowed."));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return null;
        }
        Map.Entry<String,JsonNode> entry = child.fields().next();
        String fieldName = entry.getKey();
        JsonNode value = entry.getValue();
        YangSchemaContext schemaContext = null;
        if( schemaNodeContainer instanceof YangSchemaContext){
            schemaContext= (YangSchemaContext) schemaNodeContainer;
        } else {
            SchemaNode schemaNode = (SchemaNode) schemaNodeContainer;
            schemaContext = schemaNode.getContext().getSchemaContext();
        }
        QName fieldQName = getQNameFromJsonField(fieldName,schemaContext);
        if(null == fieldQName){
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
            recordBuilder.setBadElement(child);
            recordBuilder.setErrorMessage(new ErrorMessage("unrecognized node:"+ fieldName));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return null;
        }
        SchemaNode childSchemaNode = schemaNodeContainer.getDataNodeChild(fieldQName);
        if(null == childSchemaNode){
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
            recordBuilder.setBadElement(child);
            recordBuilder.setErrorMessage(new ErrorMessage("unrecognized node:"+ fieldName));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return null;
        }
        JsonNode childJson = null;
        if(childSchemaNode instanceof MultiInstancesDataNode){
            ArrayNode arrayNode = (ArrayNode) value;
            childJson = arrayNode.get(0);
        } else {
            childJson = value;
        }

        YangDataJsonCodec<?,?> sonCodec = YangDataJsonCodec.getInstance(childSchemaNode);
        YangData<?> sonData = sonCodec.deserialize(childJson, validatorResultBuilder);
        if (null == sonData) {
            return null;
        }

        if (sonData instanceof YangDataContainer) {
            validatorResultBuilder.merge(buildChildrenData((YangDataContainer) sonData, childJson));
        }
        return sonData;
    }
    public static ValidatorResult buildChildrenData(YangDataContainer yangDataContainer, JsonNode element) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        SchemaNodeContainer schemaNodeContainer = null;
        if (yangDataContainer instanceof YangDataDocument) {
            schemaNodeContainer = ((YangDataDocument) yangDataContainer).getSchemaContext();
        } else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaNodeContainer = (SchemaNodeContainer) yangData.getSchemaNode();
        }
        Iterator<Map.Entry<String, JsonNode>> fields = element.fields();
        List<Map.Entry<String, JsonNode>> attributes = new ArrayList<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode child = field.getValue();
            JsonCodecUtil.jsonPath.put(child, fieldName);
            JsonCodecUtil.jsonNodeParent.put(child, element);
            if (fieldName.startsWith("@")) {
                attributes.add(field);
                continue;
            }
            QName qName = JsonCodecUtil.getQNameFromJsonField(fieldName,yangDataContainer);
            SchemaNode sonSchemaNode = schemaNodeContainer.getTreeNodeChild(qName);
            if (sonSchemaNode == null || !sonSchemaNode.isActive()) {
                ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.UNKNOWN_ELEMENT);
                recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
                recordBuilder.setBadElement(child);
                recordBuilder.setErrorMessage(new ErrorMessage(
                        "unrecognized element:" + fieldName));
                validatorResultBuilder.addRecord(recordBuilder.build());
                continue;
            }

            validatorResultBuilder.merge(buildChildData(yangDataContainer,child,sonSchemaNode));

        }
        for(Map.Entry<String,JsonNode> attribute:attributes){
            try {
                processAttribute(attribute.getKey(), attribute.getValue(), yangDataContainer);
            } catch (YangDataJsonCodecException e) {
                ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(e.getErrorTag());
                recordBuilder.setErrorMessage(e.getErrorMsg());
                recordBuilder.setErrorPath(e.getErrorPath());
                recordBuilder.setBadElement(e.getBadElement());
                recordBuilder.setSeverity(e.getSeverity());
                recordBuilder.setErrorAppTag(e.getErrorAppTag());
                validatorResultBuilder.addRecord(recordBuilder.build());
            }
        }
        return validatorResultBuilder.build();
    }
}
