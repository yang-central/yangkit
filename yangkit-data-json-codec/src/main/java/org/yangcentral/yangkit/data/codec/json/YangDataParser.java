package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YangDataParser {
    private JsonNode doc;

    private YangSchemaContext schemaContext;

    private boolean onlyConfig;

    public YangDataParser(JsonNode doc, YangSchemaContext schemaContext, boolean isOnlyConfig) {
        this.doc = doc;
        this.schemaContext = schemaContext;
        onlyConfig = isOnlyConfig;
        initLog4j();
    }

    private void initLog4j() {
        Properties props = new Properties();
        try {
            InputStream in = YangDataParser.class.getResourceAsStream("/log4j.properties");
            props.load(in);
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YangDataDocument parse(ValidatorResultBuilder validatorResultBuilder) {
        JsonNode data = doc.get("data");
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(schemaContext);
        YangDataDocument yangDataDocument = codec.deserialize(data, validatorResultBuilder);
        if (null != yangDataDocument) {
            //yangDataDocument.setOnlyConfig(onlyConfig);
        }
        return yangDataDocument;
    }
}
