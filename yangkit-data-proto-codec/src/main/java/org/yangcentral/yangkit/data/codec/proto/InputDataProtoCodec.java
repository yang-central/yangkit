package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.InputData;
import org.yangcentral.yangkit.data.impl.model.InputDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Input;

/** Codec for YANG RPC {@code input} data. */
public class InputDataProtoCodec extends YangDataProtoCodec<Input, InputData> {

    protected InputDataProtoCodec(Input schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected InputData buildData(DynamicMessage message,
                                  ValidatorResultBuilder validatorResultBuilder) {
        InputDataImpl data = new InputDataImpl(getSchemaNode());
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
                "No descriptor for rpc input: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
