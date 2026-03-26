package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.OutPutData;
import org.yangcentral.yangkit.data.impl.model.OutputDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Output;

/**
 * Codec for YANG RPC output data to Protocol Buffers message.
 */
public class OutputDataProtoCodec extends YangDataProtoCodec<Output, OutPutData> {

    protected OutputDataProtoCodec(Output schemaNode) {
        super(schemaNode);
    }

    @Override
    protected OutPutData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        OutputDataImpl outputData = new OutputDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        outputData.setQName(qName);
        
        // Process output content from protobuf message
        // Output contains results from RPC operations
        ProtoCodecUtil.deserializeChildren(outputData, message, validatorResultBuilder);
        
        return outputData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for output: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for output: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
