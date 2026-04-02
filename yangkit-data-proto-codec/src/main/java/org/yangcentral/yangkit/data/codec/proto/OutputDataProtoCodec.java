package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.OutPutData;
import org.yangcentral.yangkit.data.impl.model.OutputDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Output;

/** Codec for YANG RPC {@code output} data. */
public class OutputDataProtoCodec extends YangDataProtoCodec<Output, OutPutData> {

    protected OutputDataProtoCodec(Output schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected OutPutData buildData(DynamicMessage message,
                                   ValidatorResultBuilder validatorResultBuilder) {
        OutputDataImpl data = new OutputDataImpl(getSchemaNode());
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
                "No descriptor for rpc output: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
