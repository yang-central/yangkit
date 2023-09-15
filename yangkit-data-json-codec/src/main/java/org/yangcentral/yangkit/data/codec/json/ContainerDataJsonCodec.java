package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Container;


public class ContainerDataJsonCodec extends YangDataJsonCodec<Container, ContainerData> {
    public ContainerDataJsonCodec(Container schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ContainerData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        ContainerData containerData = (ContainerData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        return containerData;
    }

    @Override
    protected void buildElement(JsonNode element, YangData<?> yangData) {
    }

}
