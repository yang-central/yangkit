package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generator for Protocol Buffers schema from YANG schema.
 * Converts YANG data models to Protobuf descriptor definitions.
 */
public class ProtoSchemaGenerator {
    
    private static final String PROTOBUF_PACKAGE_PREFIX = "yangkit.proto.";
    private final Map<String, DescriptorProtos.DescriptorProto> messageCache;
    private final Map<String, Integer> fieldNumberCache;
    private final AtomicInteger messageCounter;
    
    public ProtoSchemaGenerator() {
        this.messageCache = new HashMap<>();
        this.fieldNumberCache = new HashMap<>();
        this.messageCounter = new AtomicInteger(1);
    }
    
    /**
     * Generate FileDescriptorProto for a YANG module.
     * 
     * @param module the YANG module
     * @return Protobuf FileDescriptorProto
     */
    public DescriptorProtos.FileDescriptorProto generateFileDescriptor(org.yangcentral.yangkit.model.api.stmt.Module module) {
        if (module == null) {
            return null;
        }
        
        String packageName = getProtobufPackageName(module);
        
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = 
            DescriptorProtos.FileDescriptorProto.newBuilder();
        fileBuilder.setName(packageName.replace('.', '/') + ".proto");
        fileBuilder.setPackage(packageName);
        
        // Generate messages for all data nodes in the module
        List<DataDefinition> dataNodes = getDataDefinitions(module);
        for (DataDefinition dataDef : dataNodes) {
            if (dataDef instanceof DataNode) {
                DescriptorProtos.DescriptorProto messageProto = generateMessage((DataNode) dataDef);
                if (messageProto != null) {
                    fileBuilder.addMessageType(messageProto);
                }
            }
        }
        
        return fileBuilder.build();
    }
    
    /**
     * Get data definitions from a container.
     */
    private List<DataDefinition> getDataDefinitions(DataDefContainer container) {
        if (container == null) {
            return null;
        }
        try {
            return container.getDataDefChildren();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generate a Protobuf message from a YANG data node.
     * 
     * @param dataNode the YANG data node
     * @return Protobuf DescriptorProto
     */
    public DescriptorProtos.DescriptorProto generateMessage(DataNode dataNode) {
        if (dataNode == null) {
            return null;
        }
        
        String messageName = getMessageName(dataNode);
        
        // Check cache first
        if (messageCache.containsKey(messageName)) {
            return messageCache.get(messageName);
        }
        
        DescriptorProtos.DescriptorProto.Builder messageBuilder = 
            DescriptorProtos.DescriptorProto.newBuilder();
        messageBuilder.setName(messageName);
        
        int fieldNumber = 1;
        
        // Add fields based on node type
        if (dataNode instanceof Container) {
            addContainerFields(messageBuilder, (Container) dataNode, fieldNumber);
        } else if (dataNode instanceof YangList) {
            addListFields(messageBuilder, (YangList) dataNode, fieldNumber);
        } else if (dataNode instanceof Leaf) {
            // Leaf nodes don't need their own message, they're fields in parent
            return null;
        } else if (dataNode instanceof Notification) {
            addNotificationFields(messageBuilder, (Notification) dataNode, fieldNumber);
        } else if (dataNode instanceof Rpc) {
            addRpcFields(messageBuilder, (Rpc) dataNode, fieldNumber);
        } else if (dataNode instanceof YangStructure) {
            addStructureFields(messageBuilder, (YangStructure) dataNode, fieldNumber);
        }
        
        DescriptorProtos.DescriptorProto messageProto = messageBuilder.build();
        messageCache.put(messageName, messageProto);
        
        return messageProto;
    }
    
    /**
     * Add fields for a container node.
     */
    private void addContainerFields(DescriptorProtos.DescriptorProto.Builder builder, 
                                    Container container, int startFieldNumber) {
        int fieldNumber = startFieldNumber;
        
        // Get children through DataDefContainer interface
        List<DataDefinition> children = getDataDefinitions(container);
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof Leaf) {
                    addLeafField(builder, (Leaf) child, fieldNumber++);
                } else if (child instanceof LeafList) {
                    addLeafListField(builder, (LeafList) child, fieldNumber++);
                } else if (child instanceof Container) {
                    addNestedMessageField(builder, (Container) child, fieldNumber++);
                } else if (child instanceof YangList) {
                    addNestedMessageField(builder, (YangList) child, fieldNumber++);
                }
            }
        }
    }
    
    /**
     * Add fields for a list node.
     */
    private void addListFields(DescriptorProtos.DescriptorProto.Builder builder, 
                               YangList list, int startFieldNumber) {
        int fieldNumber = startFieldNumber;
        
        // Add key fields first
        Key key = list.getKey();
        if (key != null) {
            try {
                List<Leaf> keyNodes = key.getkeyNodes();
                if (keyNodes != null) {
                    for (Leaf keyLeaf : keyNodes) {
                        addLeafField(builder, keyLeaf, fieldNumber++);
                    }
                }
            } catch (Exception e) {
                // Ignore key processing errors
            }
        }
        
        // Add other fields
        List<DataDefinition> children = getDataDefinitions(list);
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof Leaf) {
                    addLeafField(builder, (Leaf) child, fieldNumber++);
                } else if (child instanceof LeafList) {
                    addLeafListField(builder, (LeafList) child, fieldNumber++);
                } else if (child instanceof Container) {
                    addNestedMessageField(builder, (Container) child, fieldNumber++);
                }
            }
        }
    }
    
    /**
     * Add a leaf field to the message.
     */
    private void addLeafField(DescriptorProtos.DescriptorProto.Builder builder, 
                              Leaf leaf, int fieldNumber) {
        if (leaf == null) {
            return;
        }
        
        String fieldName = leaf.getIdentifier().getLocalName();
        DescriptorProtos.FieldDescriptorProto.Type protoType = getProtoType(leaf.getType());
        
        DescriptorProtos.FieldDescriptorProto.Builder fieldBuilder = 
            DescriptorProtos.FieldDescriptorProto.newBuilder();
        fieldBuilder.setName(fieldName);
        fieldBuilder.setNumber(fieldNumber);
        fieldBuilder.setType(protoType);
        fieldBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        
        builder.addField(fieldBuilder);
    }
    
    /**
     * Add a leaf-list field to the message.
     */
    private void addLeafListField(DescriptorProtos.DescriptorProto.Builder builder, 
                                  LeafList leafList, int fieldNumber) {
        if (leafList == null) {
            return;
        }
        
        String fieldName = leafList.getIdentifier().getLocalName();
        DescriptorProtos.FieldDescriptorProto.Type protoType = getProtoType(leafList.getType());
        
        DescriptorProtos.FieldDescriptorProto.Builder fieldBuilder = 
            DescriptorProtos.FieldDescriptorProto.newBuilder();
        fieldBuilder.setName(fieldName);
        fieldBuilder.setNumber(fieldNumber);
        fieldBuilder.setType(protoType);
        fieldBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
        
        builder.addField(fieldBuilder);
    }
    
    /**
     * Add a nested message field.
     */
    private void addNestedMessageField(DescriptorProtos.DescriptorProto.Builder builder, 
                                       DataNode dataNode, int fieldNumber) {
        if (dataNode == null) {
            return;
        }
        
        // First generate the nested message
        DescriptorProtos.DescriptorProto nestedMessage = generateMessage(dataNode);
        if (nestedMessage != null) {
            builder.addNestedType(nestedMessage);
        }
        
        String fieldName = dataNode.getIdentifier().getLocalName();
        String messageType = getMessageName(dataNode);
        
        DescriptorProtos.FieldDescriptorProto.Builder fieldBuilder = 
            DescriptorProtos.FieldDescriptorProto.newBuilder();
        fieldBuilder.setName(fieldName);
        fieldBuilder.setNumber(fieldNumber);
        fieldBuilder.setTypeName(messageType);
        fieldBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        
        builder.addField(fieldBuilder);
    }
    
    /**
     * Add fields for a notification node.
     */
    private void addNotificationFields(DescriptorProtos.DescriptorProto.Builder builder, 
                                       Notification notification, int startFieldNumber) {
        int fieldNumber = startFieldNumber;
        
        List<DataDefinition> children = getDataDefinitions(notification);
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof Leaf) {
                    addLeafField(builder, (Leaf) child, fieldNumber++);
                } else if (child instanceof LeafList) {
                    addLeafListField(builder, (LeafList) child, fieldNumber++);
                } else if (child instanceof Container) {
                    addNestedMessageField(builder, (Container) child, fieldNumber++);
                }
            }
        }
    }
    
    /**
     * Add fields for an RPC node.
     */
    private void addRpcFields(DescriptorProtos.DescriptorProto.Builder builder, 
                              Rpc rpc, int startFieldNumber) {
        // RPCs have input and output sub-messages
        Input input = rpc.getInput();
        if (input != null) {
            DescriptorProtos.DescriptorProto inputMessage = generateInputOutputMessage(input, rpc.getIdentifier().getLocalName() + "Input");
            if (inputMessage != null) {
                builder.addNestedType(inputMessage);
            }
        }
        
        Output output = rpc.getOutput();
        if (output != null) {
            DescriptorProtos.DescriptorProto outputMessage = generateInputOutputMessage(output, rpc.getIdentifier().getLocalName() + "Output");
            if (outputMessage != null) {
                builder.addNestedType(outputMessage);
            }
        }
    }
    
    /**
     * Generate input/output message for RPC.
     */
    private DescriptorProtos.DescriptorProto generateInputOutputMessage(DataDefContainer container, String messageName) {
        if (container == null) {
            return null;
        }
        
        DescriptorProtos.DescriptorProto.Builder messageBuilder = 
            DescriptorProtos.DescriptorProto.newBuilder();
        messageBuilder.setName(messageName);
        
        int fieldNumber = 1;
        List<DataDefinition> children = getDataDefinitions(container);
        
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof Leaf) {
                    addLeafField(messageBuilder, (Leaf) child, fieldNumber++);
                } else if (child instanceof LeafList) {
                    addLeafListField(messageBuilder, (LeafList) child, fieldNumber++);
                } else if (child instanceof Container) {
                    addNestedMessageField(messageBuilder, (Container) child, fieldNumber++);
                }
            }
        }
        
        return messageBuilder.build();
    }
    
    /**
     * Add fields for a structure node.
     */
    private void addStructureFields(DescriptorProtos.DescriptorProto.Builder builder, 
                                    YangStructure structure, int startFieldNumber) {
        int fieldNumber = startFieldNumber;
        
        List<DataDefinition> children = getDataDefinitions(structure);
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof Leaf) {
                    addLeafField(builder, (Leaf) child, fieldNumber++);
                } else if (child instanceof LeafList) {
                    addLeafListField(builder, (LeafList) child, fieldNumber++);
                } else if (child instanceof Container) {
                    addNestedMessageField(builder, (Container) child, fieldNumber++);
                }
            }
        }
    }
    
    /**
     * Get Protobuf type from YANG type.
     */
    private DescriptorProtos.FieldDescriptorProto.Type getProtoType(Type yangType) {
        if (yangType == null) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
        
        return YangProtoTypeMapper.getProtoType(yangType);
    }
    
    /**
     * Get message name from data node.
     */
    private String getMessageName(DataNode dataNode) {
        if (dataNode == null) {
            return "Unknown";
        }
        
        String localName = dataNode.getIdentifier().getLocalName();
        // Convert to PascalCase for Protobuf message names
        return capitalize(localName);
    }
    
    /**
     * Get Protobuf package name from YANG module.
     */
    private String getProtobufPackageName(org.yangcentral.yangkit.model.api.stmt.Module module) {
        if (module == null) {
            return PROTOBUF_PACKAGE_PREFIX + "unknown";
        }
        
        try {
            org.yangcentral.yangkit.model.api.schema.ModuleId moduleId = module.getModuleId();
            if (moduleId != null && moduleId.getModuleName() != null) {
                String moduleName = moduleId.getModuleName();
                return PROTOBUF_PACKAGE_PREFIX + moduleName.toLowerCase();
            }
        } catch (Exception e) {
            // Fallback to simple name
        }
        
        return PROTOBUF_PACKAGE_PREFIX + "yang_module";
    }
    
    /**
     * Capitalize first letter of string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * Clear the message cache.
     */
    public void clearCache() {
        messageCache.clear();
        fieldNumberCache.clear();
        messageCounter.set(1);
    }
}
