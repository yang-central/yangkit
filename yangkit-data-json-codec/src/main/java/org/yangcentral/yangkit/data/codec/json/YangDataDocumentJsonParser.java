package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YangDataDocumentJsonParser {
    private final YangSchemaContext schemaContext;

    public YangDataDocumentJsonParser(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        initLog4j();
    }
    private void initLog4j() {
        Properties props = new Properties();
        try {
            InputStream in = YangDataDocumentJsonParser.class.getResourceAsStream("/log4j.properties");
            props.load(in);
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YangDataDocument parse(JsonNode data, ValidatorResultBuilder validatorResultBuilder) {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(schemaContext);
        return codec.deserialize(data, validatorResultBuilder);
    }
}
