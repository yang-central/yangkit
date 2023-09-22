package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

public class YangStructureDataJsonCodec extends YangDataJsonCodec<YangStructure, YangStructureData>{
    protected YangStructureDataJsonCodec(YangStructure schemaNode) {
        super(schemaNode);
    }

    @Override
    protected YangStructureData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        YangStructureData yangStructureData = (YangStructureData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        return yangStructureData;
    }
}
