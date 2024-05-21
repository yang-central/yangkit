package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;


public class YangDataDocumentJsonWriter {
    public YangDataDocumentJsonWriter() {
        initLog4j();
    }

    private void initLog4j() {
        Properties props = new Properties();
        try {
            props.load(YangDataDocumentJsonWriter.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonNode write(YangDataDocument yangDataDocument)  {
        YangDataDocumentJsonCodec codec =
                new YangDataDocumentJsonCodec(yangDataDocument.getSchemaContext());
        JsonNode root = codec.serialize(yangDataDocument);
        return root;
    }
}