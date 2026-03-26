package org.yangcentral.yangkit.data.codec.proto.example;

import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

/**
 * Example demonstrating how to use the Protocol Buffers codec for YANG data.
 */
public class ProtoCodecExample {

    /**
     * Serialize YANG data to Protocol Buffers message.
     * 
     * @param schemaNode the YANG schema node
     * @param yangData the YANG data to serialize
     * @return Protocol Buffers message
     */
    public DynamicMessage serializeYangDataToProto(SchemaNode schemaNode, YangData<?> yangData) {
        // Get the appropriate codec for the schema node type
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(schemaNode);
        
        // Serialize YANG data to protobuf message
        DynamicMessage protoMessage = codec.serialize(yangData);
        
        return protoMessage;
    }

    /**
     * Deserialize Protocol Buffers message to YANG data.
     * 
     * @param schemaNode the YANG schema node
     * @param protoMessage the Protocol Buffers message
     * @return YANG data
     */
    public YangData<?> deserializeProtoToYangData(SchemaNode schemaNode, DynamicMessage protoMessage) {
        // Get the appropriate codec for the schema node type
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(schemaNode);
        
        // Create validator result builder
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        
        // Deserialize protobuf message to YANG data
        YangData<?> yangData = codec.deserialize(protoMessage, validatorBuilder);
        
        // Check validation results if needed
        if (!validatorBuilder.build().isOk()) {
            System.out.println("Validation warnings: " + validatorBuilder.build().getRecords());
        }
        
        return yangData;
    }

    /**
     * Example usage with container data.
     */
    public void exampleWithContainer() {
        // This is a conceptual example - actual implementation would require
        // a loaded YANG schema and instantiated data
        
        try {
            // Load schema and create data (pseudo-code)
            // SchemaNode schemaNode = loadSchemaNode("my-module:container");
            // YangData<?> containerData = createContainerData(schemaNode);
            
            // Serialize to protobuf
            // DynamicMessage protoMessage = serializeYangDataToProto(schemaNode, containerData);
            
            // Deserialize back to YANG data
            // YangData<?> restoredData = deserializeProtoToYangData(schemaNode, protoMessage);
            
            System.out.println("Proto codec example completed successfully");
        } catch (Exception e) {
            System.err.println("Error in proto codec example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProtoCodecExample example = new ProtoCodecExample();
        example.exampleWithContainer();
    }
}
