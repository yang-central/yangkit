package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.PropertyConfigurator;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
        List<XPathStep> stepList = path.getSteps();
        int size = stepList.size();
        XPathStep lastStep = stepList.get(size-1);
        JsonNode value = null;
        if(schemaNode instanceof MultiInstancesDataNode){
            ArrayNode arrayNode = (ArrayNode) data.get(schemaNode.getJsonIdentifier());
            value = arrayNode.get(0);
        } else {
            value = data.get(schemaNode.getJsonIdentifier());
        }
        YangData<?> yangData = codec.deserialize(value, validatorResultBuilder);
        if(yangData == null){
            return null;
        }
        yangData.setPath(path);
        if(yangData instanceof ListData){
            ListData listData = (ListData) yangData;
            YangList yangList = (YangList) schemaNode;
            List<Leaf> keys = yangList.getKey().getkeyNodes();
            for(Leaf key:keys){
                String keyPathValue = lastStep.getPredict(key.getIdentifier()).getValue();
                List<LeafData> leafDataList = listData.getKeys();
                LeafData keyData = null;
                for(LeafData leafData:leafDataList){
                    if(leafData.getQName().equals(key.getIdentifier())){
                        keyData = leafData;
                        break;
                    }
                }
                if(keyData == null || !keyData.getStringValue().equals(keyPathValue)){
                    ValidatorRecordBuilder validatorRecordBuilder = new ValidatorRecordBuilder();
                    validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                    validatorRecordBuilder.setBadElement(data);
                    validatorRecordBuilder.setSeverity(Severity.ERROR);
                    validatorRecordBuilder.setErrorMessage(new ErrorMessage("path:"+path + " and value:"+data + " are mismatch"));
                    validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                    return null;
                }

            }

        } else if (yangData instanceof LeafListData){
            String pathValue = lastStep.getPredict(yangData.getQName()).getValue();
            LeafListData leafListData = (LeafListData) yangData;
            if(!pathValue.equals(leafListData.getStringValue())){
                ValidatorRecordBuilder validatorRecordBuilder = new ValidatorRecordBuilder();
                validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                validatorRecordBuilder.setBadElement(data);
                validatorRecordBuilder.setSeverity(Severity.ERROR);
                validatorRecordBuilder.setErrorMessage(new ErrorMessage("path:"+path + " and value:"+data + " are mismatch"));
                validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                return null;
            }
        }
        if(yangData instanceof YangDataContainer) {
            validatorResultBuilder.merge(JsonCodecUtil.buildChildrenData((YangDataContainer) yangData, value));
        }
        return yangData;
    }
}
