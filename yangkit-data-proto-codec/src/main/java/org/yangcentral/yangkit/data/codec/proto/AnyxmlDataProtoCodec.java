package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

/**
 * Codec for YANG anyxml data to Protocol Buffers message.
 */
public class AnyxmlDataProtoCodec extends YangDataProtoCodec<Anyxml, AnyxmlData> {

    protected AnyxmlDataProtoCodec(Anyxml schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyxmlData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        AnyXmlDataImpl anyxmlData = new AnyXmlDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        anyxmlData.setQName(qName);
        
        // Process anyxml content from protobuf message
        // AnyXML contains XML content that was serialized to protobuf
        if (message != null && !message.getAllFields().isEmpty()) {
            // Store the serialized protobuf message as a string representation
            String xmlContent = message.toString();
            // For now, we just store the content as a string
            // A proper implementation would need to handle the actual XML structure
        }
        
        return anyxmlData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for anyxml: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for anyxml: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
