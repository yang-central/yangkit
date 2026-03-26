package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.impl.model.RpcDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

/**
 * Codec for YANG RPC data to Protocol Buffers message.
 */
public class RpcDataProtoCodec extends YangDataProtoCodec<Rpc, RpcData> {

    protected RpcDataProtoCodec(Rpc schemaNode) {
        super(schemaNode);
    }

    @Override
    protected RpcData buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        RpcDataImpl rpcData = new RpcDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        rpcData.setQName(qName);
        
        // Process RPC content from protobuf message
        // RPC contains both input and output
        ProtoCodecUtil.deserializeChildren(rpcData, message, validatorResultBuilder);
        
        return rpcData;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        try {
            Descriptors.Descriptor descriptor = ProtoDescriptorManager.getInstance()
                    .getDescriptor(getSchemaNode());
            
            if (descriptor == null) {
                throw new RuntimeException("Failed to get descriptor for rpc: " + getSchemaNode().getIdentifier());
            }
            
            return DynamicMessage.newBuilder(descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build protobuf message for rpc: " + getSchemaNode().getIdentifier(), e);
        }
    }
}
