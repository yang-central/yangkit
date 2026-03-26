package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

/**
 * Codec for YANG leaf-list data to Protocol Buffers message.
 */
public class LeafListDataProtoCodec extends YangDataProtoCodec<LeafList, LeafListData<?>> {

    protected LeafListDataProtoCodec(LeafList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafListData<?> buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) {
            return null;
        }
        
        // For leaf-list, we expect a single value in the message
        Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().getFields().get(0);
        if (fieldDescriptor == null) {
            return null;
        }
        
        Object value = message.getField(fieldDescriptor);
        Object yangValue = ProtoCodecUtil.convertProtoValueToYang(value, null);
        
        if (yangValue == null) {
            return null;
        }
        
        // Build YANG leaf-list data using factory
        LeafListData<?> leafListData = (LeafListData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), yangValue.toString());
        
        QName qName = getSchemaNode().getIdentifier();
        leafListData.setQName(qName);
        
        return leafListData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        LeafListData<?> leafListData = (LeafListData<?>) yangData;
        
        try {
            // Get the descriptor for this schema node
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for leaf-list: " + getSchemaNode().getIdentifier());
            }
            
            Message.Builder builder = DynamicMessage.newBuilder(descriptor);
            
            // Get the value from leaf-list data
            Object yangValue = leafListData.getStringValue();
            Object protoValue = ProtoCodecUtil.convertYangValueToProto(yangValue, null);
            
            if (protoValue != null) {
                Descriptors.FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);
                builder.setField(fieldDescriptor, protoValue);
            }
            
            return builder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for leaf-list: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
