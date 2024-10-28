package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class ExtraValidationDataJsonCodec {

    private final Map<JsonNode, String> jsonPath = new HashMap<>();
    private final Map<JsonNode, JsonNode> jsonNodeParent = new HashMap<>();
    private final Map<JsonNode, Boolean> isJsonNodeInArray = new HashMap<>();

    public ExtraValidationDataJsonCodec() {}

    public void addJsonChild(JsonNode parent, JsonNode child, String path) {
        this.jsonNodeParent.put(child, parent);
        this.jsonPath.put(child, path);
    }

    public void addJsonChildArray(JsonNode child){
        isJsonNodeInArray.put(child, true);
    }

    public String getJsonPath(JsonNode jsonNode){
        StringBuilder path = new StringBuilder();
        JsonNode parent = this.jsonNodeParent.get(jsonNode);
        while(parent != null){
            path.insert(0, this.jsonPath.get(jsonNode) + "/");
            jsonNode = parent;
            parent = this.jsonNodeParent.get(jsonNode);
        }
        path.insert(0, "/");
        path.deleteCharAt(path.length() - 1);
        return path.toString();
    }
    public boolean isNodeInJsonArray(JsonNode node){
        return this.isJsonNodeInArray.getOrDefault(node, false);
    }

}
