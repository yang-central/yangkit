package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.InputData;
import org.yangcentral.yangkit.model.api.stmt.Input;

public class InputDataJsonCodec extends YangDataJsonCodec<Input, InputData>{
    protected InputDataJsonCodec(Input schemaNode) {
        super(schemaNode);
    }

    @Override
    protected InputData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        return (InputData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),null);
    }
}
