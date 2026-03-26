package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.impl.model.NotificationDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Notification;

/**
 * Codec for YANG notification data to Protocol Buffers message.
 */
public class NotificationDataProtoCodec extends YangDataProtoCodec<Notification, NotificationData> {

    protected NotificationDataProtoCodec(Notification schemaNode) {
        super(schemaNode);
    }

    @Override
    protected NotificationData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        NotificationDataImpl notificationData = new NotificationDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        notificationData.setQName(qName);
        
        // Process notification content from protobuf message
        // Notifications contain event data
        ProtoCodecUtil.deserializeChildren(notificationData, message, validatorResultBuilder);
        
        return notificationData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for notification: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for notification: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
