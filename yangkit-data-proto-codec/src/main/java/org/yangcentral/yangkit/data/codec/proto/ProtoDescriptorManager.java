package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for Protocol Buffers descriptors.
 * Handles creation and caching of protobuf message descriptors for YANG schema nodes.
 */
public class ProtoDescriptorManager {
    
    private static volatile ProtoDescriptorManager instance;
    private final ConcurrentHashMap<String, Descriptors.Descriptor> descriptorCache;
    
    private ProtoDescriptorManager() {
        this.descriptorCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Get singleton instance of ProtoDescriptorManager.
     * 
     * @return the singleton instance
     */
    public static ProtoDescriptorManager getInstance() {
        if (instance == null) {
            synchronized (ProtoDescriptorManager.class) {
                if (instance == null) {
                    instance = new ProtoDescriptorManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get or create a protobuf descriptor for a YANG schema node.
     * 
     * @param schemaNode the YANG schema node
     * @return the corresponding protobuf descriptor
     */
    public Descriptors.Descriptor getDescriptor(SchemaNode schemaNode) {
        if (schemaNode == null) {
            return null;
        }
        
        String key = generateDescriptorKey(schemaNode);
        
        // Try to get from cache first
        Descriptors.Descriptor cached = ProtoCache.getInstance().get(key);
        if (cached != null) {
            return cached;
        }
        
        // Create and cache
        Descriptors.Descriptor descriptor = descriptorCache.computeIfAbsent(key, k -> createDescriptor(schemaNode));
        ProtoCache.getInstance().put(key, descriptor);
        
        return descriptor;
    }
    
    /**
     * Generate a unique key for a schema node.
     * 
     * @param schemaNode the schema node
     * @return unique descriptor key
     */
    private String generateDescriptorKey(SchemaNode schemaNode) {
        return schemaNode.getIdentifier().toString();
    }
    
    /**
     * Create a new descriptor for a schema node.
     * 
     * @param schemaNode the schema node
     * @return created descriptor
     */
    private Descriptors.Descriptor createDescriptor(SchemaNode schemaNode) {
        try {
            // Find root module
            org.yangcentral.yangkit.model.api.stmt.Module rootModule = findRootModule(schemaNode);
            if (rootModule == null) {
                // Try to get module from schema context
                rootModule = getModuleFromSchemaContext(schemaNode);
            }
            
            if (rootModule == null) {
                return null;
            }
            
            // Generate FileDescriptorProto for the module
            DescriptorProtos.FileDescriptorProto fileProto = generateFileDescriptorProto(rootModule);
            
            // Build FileDescriptor
            com.google.protobuf.Descriptors.FileDescriptor[] dependencies = new com.google.protobuf.Descriptors.FileDescriptor[0];
            
            com.google.protobuf.Descriptors.FileDescriptor fileDescriptor = 
                com.google.protobuf.Descriptors.FileDescriptor.buildFrom(fileProto, dependencies);
            
            // Get the message descriptor for this schema node
            String messageName = getMessageName(schemaNode);
            return fileDescriptor.findMessageTypeByName(messageName);
        } catch (Exception e) {
            // Log error and return null
            System.err.println("Failed to create descriptor for " + schemaNode.getIdentifier() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get module from schema context.
     */
    private org.yangcentral.yangkit.model.api.stmt.Module getModuleFromSchemaContext(SchemaNode schemaNode) {
        try {
            YangSchemaContext schemaContext = 
                schemaNode.getContext().getSchemaContext();
            String moduleName = schemaNode.getIdentifier().getPrefix();
            java.util.Optional<org.yangcentral.yangkit.model.api.stmt.Module> moduleOp = schemaContext.getLatestModule(moduleName);
            return moduleOp.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generate FileDescriptorProto for a YANG module.
     */
    private DescriptorProtos.FileDescriptorProto generateFileDescriptorProto(org.yangcentral.yangkit.model.api.stmt.Module module) {
        // Get namespace from MainModule
        String protoPackageName = "";
        if (module.getMainModule() != null && module.getMainModule().getNamespace() != null) {
            protoPackageName = module.getMainModule().getNamespace().getUri().toString()
                .replaceAll("[^a-zA-Z0-9_]", "_");
        } else {
            protoPackageName = module.getSelfPrefix();
        }
        
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = 
            DescriptorProtos.FileDescriptorProto.newBuilder()
                .setName(protoPackageName + ".proto")
                .setPackage(protoPackageName);
        
        // Add message types for all data nodes in the module
        for (DataNode dataNode : module.getDataNodeChildren()) {
            DescriptorProtos.DescriptorProto.Builder messageBuilder = 
                buildMessageDescriptor(dataNode);
            if (messageBuilder != null) {
                fileBuilder.addMessageType(messageBuilder);
            }
        }
        
        return fileBuilder.build();
    }
    
    /**
     * Build message descriptor for a data node.
     */
    private DescriptorProtos.DescriptorProto.Builder buildMessageDescriptor(DataNode dataNode) {
        String messageName = capitalize(dataNode.getIdentifier().getLocalName());
        DescriptorProtos.DescriptorProto.Builder builder = 
            DescriptorProtos.DescriptorProto.newBuilder()
                .setName(messageName);
        
        // Add fields based on child nodes
        int fieldNumber = 1;
        // Simplified - skip child processing for now
        // A proper implementation would need to handle the schema tree correctly
        
        return builder;
    }
    
    /**
     * Build field descriptor for a data node.
     */
    private DescriptorProtos.FieldDescriptorProto.Builder buildFieldDescriptor(DataNode dataNode, int fieldNumber) {
        String fieldName = dataNode.getIdentifier().getLocalName();
        boolean repeated = (dataNode instanceof YangList) || (dataNode instanceof LeafList);
        
        DescriptorProtos.FieldDescriptorProto.Type type = getProtoTypeForDataNode(dataNode);
        
        return DescriptorProtos.FieldDescriptorProto.newBuilder()
            .setName(fieldName)
            .setNumber(fieldNumber)
            .setType(type)
            .setLabel(repeated ? 
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED :
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
    }
    
    /**
     * Get protobuf type for a YANG data node.
     */
    private DescriptorProtos.FieldDescriptorProto.Type getProtoTypeForDataNode(DataNode dataNode) {
        if (dataNode instanceof Leaf || dataNode instanceof LeafList) {
            TypedDataNode typedNode = (TypedDataNode) dataNode;
            org.yangcentral.yangkit.model.api.stmt.Type type = typedNode.getType();
            if (type != null) {
                return YangProtoTypeMapper.getProtoType(type);
            }
        }
        
        // Default to bytes for complex types
        return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
    }
    
    /**
     * Find the root module for a schema node.
     */
    private org.yangcentral.yangkit.model.api.stmt.Module findRootModule(SchemaNode schemaNode) {
        // For now, return null as we don't have proper navigation
        // This would require understanding the YANG schema hierarchy
        return null;
    }
    
    /**
     * Get message name from schema node.
     */
    private String getMessageName(SchemaNode schemaNode) {
        String moduleName = schemaNode.getIdentifier().getPrefix();
        String localName = schemaNode.getIdentifier().getLocalName();
        
        // Capitalize first letter for message name
        return moduleName + "_" + capitalize(localName);
    }
    
    /**
     * Capitalize the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * Clear all cached descriptors.
     */
    public void clearCache() {
        descriptorCache.clear();
    }
}
