package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YangDataJsonParser {
    private final YangSchemaContext schemaContext;

    public YangDataJsonParser(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        initLog4j();
    }

    private void initLog4j() {
        Properties props = new Properties();
        try {
            InputStream in = YangDataJsonParser.class.getResourceAsStream("/log4j.properties");
            props.load(in);
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YangData<?> parse(AbsolutePath path, JsonNode data, ValidatorResultBuilder validatorResultBuilder) {
        SchemaNode schemaNode = this.schemaContext.getSchemaNode(path);
        if(schemaNode == null){
           return null;
        }
        YangDataJsonCodec<?,?> codec = YangDataJsonCodec.getInstance(schemaNode);
        YangData<?> yangData = codec.deserialize(data, validatorResultBuilder);
        if(yangData instanceof YangDataContainer) {
            validatorResultBuilder.merge(JsonCodecUtil.buildChildrenData((YangDataContainer) yangData, data));
        }
        return yangData;
    }
}
