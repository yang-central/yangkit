package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ActionData;
import org.yangcentral.yangkit.data.impl.model.ActionDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Action;

/**
 * Codec for YANG action data to Protocol Buffers message.
 */
public class ActionDataProtoCodec extends YangDataProtoCodec<Action, ActionData> {

    protected ActionDataProtoCodec(Action schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ActionData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        ActionDataImpl actionData = new ActionDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        actionData.setQName(qName);
        
        // Process action content from protobuf message
        // Actions typically have input and output parameters
        ProtoCodecUtil.deserializeChildren(actionData, message, validatorResultBuilder);
        
        return actionData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for action: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for action: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
