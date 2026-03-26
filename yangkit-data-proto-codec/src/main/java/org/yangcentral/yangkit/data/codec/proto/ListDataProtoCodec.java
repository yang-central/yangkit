package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.impl.model.ListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.YangList;

/**
 * Codec for YANG list data to Protocol Buffers message.
 */
public class ListDataProtoCodec extends YangDataProtoCodec<YangList, ListData> {

    protected ListDataProtoCodec(YangList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ListData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) {
            return null;
        }
        
        ListDataImpl listData = new ListDataImpl(getSchemaNode(), null);
        QName qName = getSchemaNode().getIdentifier();
        listData.setQName(qName);
        
        // Process list entries from protobuf message
        // This requires handling list keys and child elements
        ProtoCodecUtil.deserializeChildren(listData, message, validatorResultBuilder);
        
        return listData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            // Get the descriptor for this schema node
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for list: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for list: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
