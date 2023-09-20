package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.ArrayList;
import java.util.List;

public class ListDataJsonCodec extends YangDataJsonCodec<YangList, ListData> {
    protected ListDataJsonCodec(YangList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ListData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        //key
        List<LeafData> keyDataList = new ArrayList<>();
        List<Leaf> keys = getSchemaNode().getKey().getkeyNodes();
        for (Leaf key : keys) {
            JsonNode keyElement = element.get(key.getArgStr());
            if (keyElement == null) {
                ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.MISSING_ELEMENT);
                recordBuilder.setErrorPath(element.toString());
                recordBuilder.setBadElement(element);
                recordBuilder.setErrorMessage(new ErrorMessage("missing key:" + key.getIdentifier().getLocalName()));
                validatorResultBuilder.addRecord(recordBuilder.build());
                return null;
            }
            YangDataJsonCodec jsonCodec = YangDataJsonCodec.getInstance(key);
            LeafData keyData = (LeafData) jsonCodec.deserialize(keyElement, validatorResultBuilder);
            keyDataList.add(keyData);
        }
        ListData listData = (ListData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), keyDataList);
        return listData;
    }


}