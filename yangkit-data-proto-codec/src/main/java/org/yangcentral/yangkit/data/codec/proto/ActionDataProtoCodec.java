package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ActionData;
import org.yangcentral.yangkit.data.impl.model.ActionDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Action;

/** Codec for YANG {@code action} data. */
public class ActionDataProtoCodec extends YangDataProtoCodec<Action, ActionData> {

    protected ActionDataProtoCodec(Action schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected ActionData buildData(DynamicMessage message,
                                   ValidatorResultBuilder validatorResultBuilder) {
        ActionDataImpl data = new ActionDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder);
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for action: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
