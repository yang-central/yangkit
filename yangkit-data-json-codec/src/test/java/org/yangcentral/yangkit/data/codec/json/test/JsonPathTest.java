package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.data.codec.json.ExtraValidationDataJsonCodec;
import org.yangcentral.yangkit.data.codec.json.ExtraValidationDataContext;
import org.yangcentral.yangkit.data.codec.json.JsonCodecUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for JsonCodecUtil.getJsonPath() method.
 */
public class JsonPathTest {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Test
    public void testGetJsonPathWithRootAndTarget() throws Exception {
        // Create a JSON tree: {"module:container": {"name": "test", "value": 42}}
        String jsonStr = "{\"module:container\": {\"name\": \"test\", \"value\": 42}}";
        JsonNode rootNode = MAPPER.readTree(jsonStr);
        
        // Get the target node (name field)
        JsonNode containerNode = rootNode.get("module:container");
        JsonNode nameNode = containerNode.get("name");
        
        // Test path generation
        String path = JsonCodecUtil.getJsonPath(rootNode, nameNode);
        assertEquals("/module:container/name", path, "Path should point to name field");
        
        // Test value field path
        JsonNode valueNode = containerNode.get("value");
        String valuePath = JsonCodecUtil.getJsonPath(rootNode, valueNode);
        assertEquals("/module:container/value", valuePath, "Path should point to value field");
    }
    
    @Test
    public void testGetJsonPathWithArray() throws Exception {
        // Create a JSON tree with array: {"items": [{"id": 1}, {"id": 2}]}
        String jsonStr = "{\"items\": [{\"id\": 1}, {\"id\": 2}]}";
        JsonNode rootNode = MAPPER.readTree(jsonStr);
        
        // Get the first item
        JsonNode itemsNode = rootNode.get("items");
        JsonNode firstItem = itemsNode.get(0);
        JsonNode idNode = firstItem.get("id");
        
        // Test path generation through array
        String path = JsonCodecUtil.getJsonPath(rootNode, idNode);
        assertEquals("/items[0]/id", path, "Path should include array index");
    }
    
    @Test
    public void testGetJsonPathNullHandling() throws Exception {
        // Test with null root
        String jsonStr = "{\"field\": \"value\"}";
        JsonNode node = MAPPER.readTree(jsonStr);
        
        String path1 = JsonCodecUtil.getJsonPath(null, node);
        assertEquals("", path1, "Should return empty string for null root");
        
        // Test with null target
        String path2 = JsonCodecUtil.getJsonPath(node, null);
        assertEquals("", path2, "Should return empty string for null target");
        
        // Test with both null
        String path3 = JsonCodecUtil.getJsonPath(null, null);
        assertEquals("", path3, "Should return empty string for both null");
    }
    
    @Test
    public void testGetJsonPathRootNode() throws Exception {
        String jsonStr = "{\"field\": \"value\"}";
        JsonNode rootNode = MAPPER.readTree(jsonStr);
        
        // Path from root to itself should be "/"
        String path = JsonCodecUtil.getJsonPath(rootNode, rootNode);
        assertEquals("/", path, "Root node path should be '/'");
    }
    
    @Test
    public void testGetJsonPathNestedStructure() throws Exception {
        // Create deeply nested structure
        String jsonStr = "{\n" +
            "\"module:root\": {\n" +
            "  \"container\": {\n" +
            "    \"list\": [\n" +
            "      {\"name\": \"item1\"},\n" +
            "      {\"name\": \"item2\"}\n" +
            "    ]\n" +
            "  }\n" +
            "}\n" +
        "}";
        
        JsonNode rootNode = MAPPER.readTree(jsonStr);
        JsonNode secondItem = rootNode
            .get("module:root")
            .get("container")
            .get("list")
            .get(1);
        JsonNode nameNode = secondItem.get("name");
        
        String path = JsonCodecUtil.getJsonPath(rootNode, nameNode);
        assertEquals("/module:root/container/list[1]/name", path, 
            "Path should handle deeply nested structures");
    }
    
    @Test
    public void testGetJsonPathSingleParameter() throws Exception {
        // Test the single-parameter version without context
        String jsonStr = "{\"field\": \"value\"}";
        JsonNode node = MAPPER.readTree(jsonStr);
        
        String path = JsonCodecUtil.getJsonPath(node);
        // Without context, should return empty string or placeholder
        // Both are acceptable behaviors
        assertTrue(path.isEmpty() || path.startsWith("<path-"), 
            "Single parameter version should return empty or placeholder when no context, got: " + path);
    }
    
    @Test
    public void testGetJsonPathWithContext() throws Exception {
        // Test that JSON path works correctly during deserialization with context
        String jsonStr = "{\"module:container\": {\"name\": \"test\", \"value\": 42}}";
        JsonNode rootNode = MAPPER.readTree(jsonStr);
        
        // Create validation context and set it
        ExtraValidationDataJsonCodec context = new ExtraValidationDataJsonCodec();
        ExtraValidationDataContext.setCurrentContext(context);
        
        try {
            // Simulate recording parent-child relationships during deserialization
            JsonNode containerNode = rootNode.get("module:container");
            JsonNode nameNode = containerNode.get("name");
            
            // Record the relationship (this is what happens during actual deserialization)
            context.addJsonChild(rootNode, containerNode, "module:container");
            context.addJsonChild(containerNode, nameNode, "name");
            
            // Now getJsonPath should work with just the target node
            String path = JsonCodecUtil.getJsonPath(nameNode);
            assertEquals("/module:container/name", path, 
                "Path should be generated correctly using context");
        } finally {
            ExtraValidationDataContext.clearContext();
        }
    }
}
