package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ActionData;
import org.yangcentral.yangkit.model.api.stmt.Action;

public class ActionDataJsonCodec extends YangDataJsonCodec<Action, ActionData>{
    protected ActionDataJsonCodec(Action schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ActionData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        return (ActionData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),null);
    }
}
