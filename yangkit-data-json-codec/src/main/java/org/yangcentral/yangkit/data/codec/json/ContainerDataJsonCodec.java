package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.model.api.stmt.Container;


public class ContainerDataJsonCodec extends YangDataJsonCodec<Container, ContainerData> {
    public ContainerDataJsonCodec(Container schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ContainerData buildData(JsonNode jsonNode , ValidatorResultBuilder validatorResultBuilder) {
        return (ContainerData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
    }


}
