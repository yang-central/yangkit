package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

public class RpcDataJsonCodec extends YangDataJsonCodec<Rpc, RpcData>{
    protected RpcDataJsonCodec(Rpc schemaNode) {
        super(schemaNode);
    }

    @Override
    protected RpcData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        RpcData rpcData = (RpcData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        return rpcData;
    }

}
