package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.impl.model.RpcDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

/** Codec for YANG {@code rpc} data. */
public class RpcDataProtoCodec extends YangDataProtoCodec<Rpc, RpcData> {

    protected RpcDataProtoCodec(Rpc schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected RpcData buildData(DynamicMessage message,
                                ValidatorResultBuilder validatorResultBuilder) {
        RpcDataImpl data = new RpcDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder, mode,
                getAnydataValidationContextResolver(), getSourcePath());
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for rpc: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
