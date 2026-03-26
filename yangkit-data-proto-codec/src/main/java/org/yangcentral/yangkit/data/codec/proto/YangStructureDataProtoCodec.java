package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.data.impl.model.YangStructureDataImpl;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

/**
 * Codec for YANG structure data to Protocol Buffers message.
 */
public class YangStructureDataProtoCodec extends YangDataProtoCodec<YangStructure, YangStructureData> {

    protected YangStructureDataProtoCodec(YangStructure schemaNode) {
        super(schemaNode);
    }

    @Override
    protected YangStructureData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        YangStructureDataImpl structureData = new YangStructureDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        structureData.setQName(qName);
        
        // Process structure content from protobuf message
        // Structures contain nested data nodes
        ProtoCodecUtil.deserializeChildren(structureData, message, validatorResultBuilder);
        
        return structureData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for structure: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for structure: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
