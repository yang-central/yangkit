package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.InputData;
import org.yangcentral.yangkit.data.impl.model.InputDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Input;

/**
 * Codec for YANG RPC input data to Protocol Buffers message.
 */
public class InputDataProtoCodec extends YangDataProtoCodec<Input, InputData> {

    protected InputDataProtoCodec(Input schemaNode) {
        super(schemaNode);
    }

    @Override
    protected InputData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        InputDataImpl inputData = new InputDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        inputData.setQName(qName);
        
        // Process input content from protobuf message
        // Input contains parameters for RPC operations
        ProtoCodecUtil.deserializeChildren(inputData, message, validatorResultBuilder);
        
        return inputData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for input: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for input: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
