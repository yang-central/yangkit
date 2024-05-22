package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.units.qual.A;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public  class AttributeBlock {
    private JsonNode jsonNode;
    private YangSchemaContext schemaContext;
    private List<Attribute> attributes = new ArrayList<>();
    private List<AttributeBlock> children = new ArrayList<>();

    public AttributeBlock(JsonNode jsonNode, YangSchemaContext schemaContext) {
        this.jsonNode = jsonNode;
        this.schemaContext = schemaContext;
        parse();
    }
    private void jsonObjectAddAttribute(JsonNode attributeValue) {
        if(attributeValue.isNull()){
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> childEntries = attributeValue.fields();
        while (childEntries.hasNext()) {
            Map.Entry<String, JsonNode> childEntry = childEntries.next();
            String childEntryKey = childEntry.getKey();
            QName qName = JsonCodecUtil.getQNameFromJsonField(childEntryKey,schemaContext);
            String attr = childEntry.getValue().asText();
            Attribute attribute = new Attribute(qName, attr);
            attributes.add(attribute);
        }

    }
    private void parse(){
        if (jsonNode.isObject()) {
            jsonObjectAddAttribute(jsonNode);
        } else if (jsonNode.isArray()) {
            ArrayNode attributeArray = (ArrayNode) jsonNode;
            for (int i = 0; i < attributeArray.size(); i++) {
                JsonNode childElement = attributeArray.get(i);
                AttributeBlock child = new AttributeBlock(childElement,schemaContext);
                children.add(child);
            }
        }
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<AttributeBlock> getChildren() {
        return children;
    }
}
