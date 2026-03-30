package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.impl.model.NotificationDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Notification;

/** Codec for YANG {@code notification} data. */
public class NotificationDataProtoCodec extends YangDataProtoCodec<Notification, NotificationData> {

    protected NotificationDataProtoCodec(Notification schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected NotificationData buildData(DynamicMessage message,
                                          ValidatorResultBuilder validatorResultBuilder) {
        NotificationDataImpl data = new NotificationDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder);
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for notification: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
