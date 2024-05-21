package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class YangDataJsonWriter {

    public YangDataJsonWriter() {
        initLog4j();
    }

    private void initLog4j() {
        Properties props = new Properties();
        try {
            props.load(YangDataJsonWriter.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonNode write(AbsolutePath path, YangData<?> yangData)  {
        YangDataJsonCodec<?,?> codec = YangDataJsonCodec.getInstance(yangData.getSchemaNode());
        JsonNode root = codec.serialize(yangData);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SchemaNode schemaNode = yangData.getSchemaNode();
        ObjectNode objectNode = mapper.createObjectNode();
        if(schemaNode instanceof MultiInstancesDataNode){
            ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
            arrayNode.add(root);
            objectNode.put(schemaNode.getJsonIdentifier(),arrayNode);
        } else {
            objectNode.put(schemaNode.getJsonIdentifier(),root);
        }
        return objectNode;
    }
}
