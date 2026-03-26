package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

/**
 * Codec for YANG leaf data to Protocol Buffers message.
 */
public class LeafDataProtoCodec extends YangDataProtoCodec<Leaf, LeafData<?>> {

    protected LeafDataProtoCodec(Leaf schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafData<?> buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) {
            return null;
        }
        
        // Get the first field value from the message
        Descriptors.FieldDescriptor field = message.getDescriptorForType().getFields().get(0);
        Object value = message.getField(field);
        
        // Convert protobuf value to YANG value
        Object yangValue = ProtoCodecUtil.convertProtoValueToYang(value, null);
        
        // Build YANG leaf data using factory
        LeafData<?> leafData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), yangValue != null ? yangValue.toString() : null);
        
        QName qName = getSchemaNode().getIdentifier();
        leafData.setQName(qName);
        
        return leafData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        LeafData<?> leafData = (LeafData<?>) yangData;
        
        // Get string value from leaf
        String value = leafData.getStringValue();
        
        // Create DynamicMessage builder
        Descriptors.Descriptor descriptor = getDescriptor();
        if (descriptor == null) {
            return null;
        }
        
        Message.Builder builder = DynamicMessage.newBuilder(descriptor);
        
        // Set the field value
        if (descriptor.getFields().size() > 0 && value != null) {
            Descriptors.FieldDescriptor field = descriptor.getFields().get(0);
            Object protoValue = ProtoCodecUtil.convertYangValueToProto(value, null);
            builder.setField(field, protoValue);
        }
        
        return builder;
    }
    
    private Descriptors.Descriptor getDescriptor() {
        // Get descriptor from ProtoDescriptorManager
        return ProtoDescriptorManager.getInstance().getDescriptor(getSchemaNode());
    }
}
