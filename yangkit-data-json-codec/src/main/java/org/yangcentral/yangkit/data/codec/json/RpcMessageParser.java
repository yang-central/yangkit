package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class RpcMessageParser {
    private final YangSchemaContext schemaContext;

    public RpcMessageParser(YangSchemaContext schemaContext) {
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

    /**
     *
     * @param path  must be module:operation format
     * @param input  JSON node, must be input
     * @param validatorResultBuilder
     * @return
     */
    public YangDataDocument parseInput(String path,JsonNode input, ValidatorResultBuilder validatorResultBuilder) {
        FName fName = new FName(path.substring(1));
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put(fName.toString(),input);
        RpcDocumentJsonCodec codec = new RpcDocumentJsonCodec(schemaContext);
        return codec.deserialize(root, validatorResultBuilder);
    }

    public ValidatorResult parseOutPut(YangDataDocument rpcDoc,JsonNode output){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        List<YangData<?>> children =  rpcDoc.getChildren();
        if(children.isEmpty() || children.size() >1){
            ValidatorRecordBuilder validatorRecordBuilder = new ValidatorRecordBuilder<>();
            validatorRecordBuilder.setErrorMessage(new ErrorMessage("rpc doc must have only one root node named rpc name"));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
        }
        RpcData rpcData = (RpcData) children.get(0);
        validatorResultBuilder.merge(JsonCodecUtil.buildChildrenData(rpcData, output));
        return validatorResultBuilder.build();
    }
}
