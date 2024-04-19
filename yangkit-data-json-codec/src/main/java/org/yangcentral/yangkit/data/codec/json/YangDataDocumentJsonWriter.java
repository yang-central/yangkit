package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;


public class YangDataDocumentJsonWriter {
    private YangDataDocument yangDataDocument;

    private OutputStream out;

    public YangDataDocumentJsonWriter(YangDataDocument yangDataDocument, OutputStream out) {
        this.yangDataDocument = yangDataDocument;
        this.out = out;
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

    public void write() throws IOException {
        YangDataDocumentJsonCodec codec =
                new YangDataDocumentJsonCodec(yangDataDocument.getSchemaContext());
        JsonNode root = codec.serialize(yangDataDocument);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(root);
        out.write(json.getBytes());
        out.flush();
        out.close();
    }
}