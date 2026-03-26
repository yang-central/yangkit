package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;

/**
 * Codec for YANG container data to Protocol Buffers message.
 */
public class ContainerDataProtoCodec extends YangDataProtoCodec<Container, ContainerData> {

    protected ContainerDataProtoCodec(Container schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ContainerData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) {
            return null;
        }
        
        ContainerDataImpl containerData = new ContainerDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        containerData.setQName(qName);
        
        // Process child elements from protobuf message
        ProtoCodecUtil.deserializeChildren(containerData, message, validatorResultBuilder);
        
        return containerData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            // Get the descriptor for this schema node
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for container: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for container: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
