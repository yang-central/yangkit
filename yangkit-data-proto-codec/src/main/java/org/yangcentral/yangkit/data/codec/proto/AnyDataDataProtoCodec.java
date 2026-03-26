package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

/**
 * Codec for YANG anydata data to Protocol Buffers message.
 */
public class AnyDataDataProtoCodec extends YangDataProtoCodec<Anydata, AnyDataData> {

    protected AnyDataDataProtoCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyDataData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        AnyDataDataImpl anyDataData = new AnyDataDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        anyDataData.setQName(qName);
        
        // Process anydata content from protobuf message
        // AnyData can contain arbitrary XML or JSON content
        if (message != null && !message.getAllFields().isEmpty()) {
            // Store the serialized protobuf message as a string representation
            String content = message.toString();
            // For now, we just store the content as a string
            // A proper implementation would need to handle the actual data structure
        }
        
        return anyDataData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for anydata: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for anydata: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
