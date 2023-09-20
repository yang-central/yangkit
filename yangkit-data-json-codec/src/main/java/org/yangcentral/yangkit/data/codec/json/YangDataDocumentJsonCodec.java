package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataDocumentCodec;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangDataEntity;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.*;

public class YangDataDocumentJsonCodec implements YangDataDocumentCodec<JsonNode> {

    private YangSchemaContext yangSchemaContext;

    private AttributeCache attributeCache = new AttributeCache();

    public YangDataDocumentJsonCodec(YangSchemaContext yangSchemaContext) {
        this.yangSchemaContext = yangSchemaContext;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return yangSchemaContext;
    }

    public QName getQNameFromKey(String value) {
        FName fName = new FName(value);
        String moduleName = fName.getPrefix();
        if (moduleName == null) {
            return null;
        }
        String localName = fName.getLocalName();
        Optional<Module> optionalModule = yangSchemaContext.getLatestModule(moduleName);
        if (optionalModule.isPresent()) {
            Namespace namespace = ModelUtil.getNamespace(optionalModule.get());
            QName qName = new QName(namespace, localName);
            return qName;
        } else {
            return null;
        }
    }

    private void processAttribute(String key, JsonNode attributeValue, YangDataContainer yangDataContainer) {
        List<Attribute> attributeList = new ArrayList<>();
        if (key.equals("@")) {
            attributeList = getAttributeList(attributeValue);
            if (attributeList.size() > 0) {
                for (Attribute attribute : attributeList) {
                    ((YangDataEntity) yangDataContainer).addAttribute(attribute);
                }
            }
        } else {
            String ckey = key.substring(1);
            QName qName = getQNameFromKey(ckey);
            attributeList = getAttributeList(attributeValue);
            if (yangDataContainer.getDataChildren(qName) == null) {
                attributeCache.put(qName, attributeList);
            }
            if (attributeList.size() > 0) {

                List<YangData<?>> children = yangDataContainer.getDataChildren(qName);
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).addAttribute(attributeList.get(i));
                }
            }
        }
    }

    public void jsonObjectAddAttribute(JsonNode attributeValue, List<Attribute> list) {
        Iterator<Map.Entry<String, JsonNode>> childEntries = attributeValue.fields();
        while (childEntries.hasNext()) {
            Map.Entry<String, JsonNode> childEntry = childEntries.next();
            String childEntryKey = childEntry.getKey();
            QName qName = getQNameFromKey(childEntryKey);
            String attr = childEntry.getValue().asText();
            Attribute attribute = new Attribute(qName, attr);
            list.add(attribute);
        }

    }


    public List<Attribute> getAttributeList(JsonNode attributeValue) {
        List<Attribute> attributeList = new ArrayList<>();
        if (attributeValue.isObject()) {
            jsonObjectAddAttribute(attributeValue, attributeList);
        } else if (attributeValue.isArray()) {
            ArrayNode attributeArray = (ArrayNode) attributeValue;
            for (int i = 0; i < attributeArray.size(); i++) {
                JsonNode childElement = attributeArray.get(i);
                if (childElement.isNull()) {
                    continue;
                } else if (childElement.isObject()) {
                    ObjectNode attributeObject = (ObjectNode) childElement;
                    jsonObjectAddAttribute(attributeObject, attributeList);
                }
            }
        }
        return attributeList;
    }

    private ValidatorResult buildChildData(YangDataContainer yangDataContainer,JsonNode child,SchemaNode childSchemaNode){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
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

    protected ValidatorResult buildChildrenData(YangDataContainer yangDataContainer, JsonNode element) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        SchemaNodeContainer schemaNodeContainer = null;
        if (yangDataContainer instanceof YangDataDocument) {
            schemaNodeContainer = ((YangDataDocument) yangDataContainer).getSchemaContext();
        } else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaNodeContainer = (SchemaNodeContainer) yangData.getSchemaNode();
        }
        Iterator<Map.Entry<String, JsonNode>> fields = element.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode child = field.getValue();
            QName qName = getQNameFromKey(fieldName);
            if (fieldName.startsWith("@")) {
                processAttribute(fieldName, child, yangDataContainer);
                continue;
            }
            SchemaNode sonSchemaNode = schemaNodeContainer.getTreeNodeChild(qName);
            if (sonSchemaNode == null || !sonSchemaNode.isActive()) {
                ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.UNKNOWN_ELEMENT);
                recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
                recordBuilder.setBadElement(child);
                recordBuilder.setErrorMessage(new ErrorMessage(
                        "unrecognized element:" + child.toString()));
                validatorResultBuilder.addRecord(recordBuilder.build());
                continue;
            }
            if(child.isArray()) {
                if((sonSchemaNode instanceof YangList) || (sonSchemaNode instanceof LeafList)) {
                   int size = child.size();
                   for (int i =0;i < size;i++) {
                       JsonNode childElement = child.get(i);
                       validatorResultBuilder.merge(buildChildData(yangDataContainer,childElement,sonSchemaNode));
                   }
                } else {
                    ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                    recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                    recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(child));
                    recordBuilder.setBadElement(child);
                    recordBuilder.setErrorMessage(new ErrorMessage(
                            "bad element:" + child.toString()));
                    validatorResultBuilder.addRecord(recordBuilder.build());
                }
            } else {
                validatorResultBuilder.merge(buildChildData(yangDataContainer,child,sonSchemaNode));
            }

        }
        return validatorResultBuilder.build();
    }


    @Override
    public YangDataDocument deserialize(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        if (element.isNull()) {
            return null;
        }
        int size = 0;
        Iterator<String> fields = element.fieldNames();
        String rootName = null;
        while (fields.hasNext()) {
            String field = fields.next();
            size++;
            rootName = field;
        }
        if(size == 0) {
            return null;
        }
        if(size > 1) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(element));
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorMessage(new ErrorMessage("wrong format, only one root node can be allowed."));
            validatorResultBuilder.addRecord(recordBuilder.build());
        }
        JsonNode rootNode = element.get(rootName);
        YangDataDocument yangDataDocument = new YangDataDocumentImpl(
                JsonCodecUtil.getQNameFromJsonField(rootName,yangSchemaContext), yangSchemaContext);
        YangDataContainer yangDataContainer = yangDataDocument;
        validatorResultBuilder.merge(buildChildrenData(yangDataContainer, rootNode));
        return yangDataDocument;
    }

    @Override
    public JsonNode serialize(YangDataDocument yangDataDocument) {
        if (null == yangDataDocument) {
            return null;
        }
        List<Attribute> list = yangDataDocument.getAttributes();
        String fieldName = "data";
        if(yangDataDocument.getQName() != null) {
            fieldName = JsonCodecUtil.getJsonFieldFromQName(yangDataDocument.getQName(),getSchemaContext());
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode docRoot = mapper.createObjectNode();
        root.put(fieldName, docRoot);
        List<YangData<?>> children = yangDataDocument.getDataChildren();
        if (null == children) {
            return root;
        }
        for (YangData child : children) {
            if (null == child || child.isDummyNode()) {
                continue;
            }
            JsonNode childElement = YangDataJsonCodec
                    .getInstance(child.getSchemaNode())
                    .serialize(child);
            if((child.getSchemaNode() instanceof YangList)
            || (child.getSchemaNode() instanceof LeafList)) {
                ArrayNode arrayNode = (ArrayNode) docRoot.get(child.getSchemaNode().getJsonIdentifier());
                if(arrayNode == null) {
                    arrayNode = mapper.createArrayNode();
                    docRoot.put(child.getSchemaNode().getJsonIdentifier(),arrayNode);
                }
                arrayNode.add(childElement);
            } else {
                docRoot.put(child.getSchemaNode().getJsonIdentifier(), childElement);
            }
        }
        return root;
    }


}