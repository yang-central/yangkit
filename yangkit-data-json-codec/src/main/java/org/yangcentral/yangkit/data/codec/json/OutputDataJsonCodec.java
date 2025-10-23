package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.OutPutData;
import org.yangcentral.yangkit.model.api.stmt.Output;

public class OutputDataJsonCodec extends YangDataJsonCodec<Output, OutPutData>{
    protected OutputDataJsonCodec(Output schemaNode) {
        super(schemaNode);
    }

    @Override
    protected OutPutData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        return (OutPutData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),null);
    }
}
