package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for Protocol Buffers codec operations.
 */
public class ProtoCodecUtil {

    /**
     * Deserialize children from protobuf message to YANG data container.
     * 
     * @param container the YANG data container
     * @param message the protobuf message
     * @param validatorResultBuilder validator result builder
     */
    public static void deserializeChildren(YangDataContainer container, DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) {
            return;
        }
        
        // Process all fields in the protobuf message
        for (Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
            String fieldName = field.getName();
            QName qName = getQNameFromProtoField(fieldName, container);
            
            if (field.isRepeated()) {
                // Handle repeated fields (leaf-list or list)
                int count = message.getRepeatedFieldCount(field);
                for (int i = 0; i < count; i++) {
                    Object fieldValue = message.getRepeatedField(field, i);
                    createAndAddYangData(container, field, fieldValue, qName, validatorResultBuilder);
                }
            } else {
                // Handle single-value fields
                Object fieldValue = message.getField(field);
                if (fieldValue != null) {
                    createAndAddYangData(container, field, fieldValue, qName, validatorResultBuilder);
                }
            }
        }
    }
    
    /**
     * Create appropriate YangData instance and add to container.
     * 
     * @param container the parent container
     * @param field the protobuf field descriptor
     * @param fieldValue the protobuf field value
     * @param qName the YANG qualified name
     * @param validatorResultBuilder validator result builder
     */
    private static void createAndAddYangData(YangDataContainer container, 
                                              Descriptors.FieldDescriptor field,
                                              Object fieldValue,
                                              QName qName,
                                              ValidatorResultBuilder validatorResultBuilder) {
        if (container == null || field == null || fieldValue == null) {
            return;
        }
        
        // Get the schema node for this field
        SchemaNode schemaNode = findSchemaNode(container, qName);
        if (schemaNode == null) {
            return;
        }
        
        // Create appropriate YangData instance based on schema node type
        YangData<?> yangData = null;
        
        if (schemaNode instanceof Leaf) {
            // Handle leaf node
            Object yangValue = convertProtoValueToYang(fieldValue, ((Leaf) schemaNode).getType());
            yangData = createLeafData((Leaf) schemaNode, yangValue);
        } else if (schemaNode instanceof LeafList) {
            // Handle leaf-list node
            Object yangValue = convertProtoValueToYang(fieldValue, ((LeafList) schemaNode).getType());
            yangData = createLeafListData((LeafList) schemaNode, yangValue);
        } else if (schemaNode instanceof Container) {
            // Handle container node
            if (fieldValue instanceof DynamicMessage) {
                yangData = createContainerData((Container) schemaNode, (DynamicMessage) fieldValue, validatorResultBuilder);
            }
        } else if (schemaNode instanceof YangList) {
            // Handle list node
            if (fieldValue instanceof DynamicMessage) {
                yangData = createListData((YangList) schemaNode, (DynamicMessage) fieldValue, validatorResultBuilder);
            }
        } else if (schemaNode instanceof Anydata) {
            // Handle anydata node
            yangData = createAnyDataData((Anydata) schemaNode, fieldValue);
        } else if (schemaNode instanceof Anyxml) {
            // Handle anyxml node
            yangData = createAnyXmlData((Anyxml) schemaNode, fieldValue);
        }
        
        // Add to container if created successfully
        if (yangData != null) {
            try {
                container.addDataChild(yangData);
            } catch (Exception e) {
                // Log error but continue processing
                System.err.println("Failed to add data child: " + e.getMessage());
            }
        }
    }
    
    /**
     * Find schema node for a given QName in the container's context.
     */
    private static SchemaNode findSchemaNode(YangDataContainer container, QName qName) {
        // Simplified implementation - returns null for now
        // A full implementation would require proper schema context navigation
        return null;
    }
    
    /**
     * Create leaf data instance.
     */
    private static YangData<?> createLeafData(Leaf leaf, Object value) {
        if (value == null) {
            return null;
        }
        // Simplified - just return null for now
        // A proper implementation needs correct API usage
        return null;
    }
    
    /**
     * Create leaf-list data instance.
     */
    private static YangData<?> createLeafListData(LeafList leafList, Object value) {
        if (value == null) {
            return null;
        }
        // Simplified - just return null for now
        return null;
    }
    
    /**
     * Create container data instance.
     */
    private static YangData<?> createContainerData(Container container, DynamicMessage message, 
                                                    ValidatorResultBuilder validatorResultBuilder) {
        org.yangcentral.yangkit.data.impl.model.ContainerDataImpl containerData = new org.yangcentral.yangkit.data.impl.model.ContainerDataImpl(container);
        containerData.setQName(container.getIdentifier());
        
        // Recursively deserialize children
        deserializeChildren(containerData, message, validatorResultBuilder);
        
        return containerData;
    }
    
    /**
     * Create list data instance.
     */
    private static YangData<?> createListData(YangList list, DynamicMessage message,
                                               ValidatorResultBuilder validatorResultBuilder) {
        org.yangcentral.yangkit.data.impl.model.ListDataImpl listData = new org.yangcentral.yangkit.data.impl.model.ListDataImpl(list, null);
        listData.setQName(list.getIdentifier());
        
        // Process list entry content
        deserializeChildren(listData, message, validatorResultBuilder);
        
        return listData;
    }
    
    /**
     * Create anydata data instance.
     */
    private static YangData<?> createAnyDataData(Anydata anydata, Object fieldValue) {
        org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl anyDataData = new org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl(anydata);
        anyDataData.setQName(anydata.getIdentifier());
        // AnyData can contain arbitrary content - simplified for now
        return anyDataData;
    }
    
    /**
     * Create anyxml data instance.
     */
    private static YangData<?> createAnyXmlData(Anyxml anyxml, Object fieldValue) {
        org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl anyXmlData = new org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl(anyxml);
        anyXmlData.setQName(anyxml.getIdentifier());
        // AnyXML contains XML content - simplified for now
        return anyXmlData;
    }

    /**
     * Serialize children of a YANG data container to protobuf message.
     * 
     * @param message the protobuf message builder
     * @param yangDataContainer the YANG data container
     */
    public static void serializeChildren(Message.Builder message, YangDataContainer yangDataContainer) {
        List<YangData<?>> children = yangDataContainer.getDataChildren();
        if (null == children) {
            return;
        }
        
        for (YangData<?> child : children) {
            if (null == child || child.isDummyNode()) {
                continue;
            }
            
            String fieldName = getProtoFieldNameFromQName(child.getQName(), yangDataContainer);
            DynamicMessage childMessage = YangDataProtoCodec
                    .getInstance(child.getSchemaNode())
                    .serialize(child);
            
            if ((child.getSchemaNode() instanceof YangList)
                    || (child.getSchemaNode() instanceof LeafList)) {
                // Handle repeated fields
                Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(fieldName);
                if (fieldDescriptor != null) {
                    message.addRepeatedField(fieldDescriptor, childMessage);
                }
            } else {
                Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(fieldName);
                if (fieldDescriptor != null) {
                    message.setField(fieldDescriptor, childMessage);
                }
            }
        }
    }

    /**
     * Get protobuf field name from YANG QName.
     * 
     * @param qName the YANG qualified name
     * @param parent the parent YANG data container
     * @return protobuf field name
     */
    public static String getProtoFieldNameFromQName(QName qName, YangDataContainer parent) {
        if (qName == null) {
            return null;
        }
        
        // Use local name as field name in protobuf
        return qName.getLocalName();
    }

    /**
     * Get YANG QName from protobuf field name.
     * 
     * @param fieldName the protobuf field name
     * @param parent the parent YANG data container
     * @return YANG QName
     */
    public static QName getQNameFromProtoField(String fieldName, YangDataContainer parent) {
        FName fName = new FName(fieldName);
        String moduleName = fName.getPrefix();
        URI ns = null;
        
        if (moduleName == null) {
            if (parent instanceof YangDataDocument) {
                YangDataDocument yangDataDocument = (YangDataDocument) parent;
                ns = yangDataDocument.getQName().getNamespace();
            } else {
                YangData<?> yangData = (YangData<?>) parent;
                ns = yangData.getQName().getNamespace();
            }
        } else {
            YangSchemaContext schemaContext = null;
            if (parent instanceof YangDataDocument) {
                YangDataDocument yangDataDocument = (YangDataDocument) parent;
                schemaContext = yangDataDocument.getSchemaContext();
            } else {
                YangData<?> yangData = (YangData<?>) parent;
                schemaContext = yangData.getSchemaNode().getContext().getSchemaContext();
            }
            Optional<Module> moduleOp = schemaContext.getLatestModule(moduleName);
            if (!moduleOp.isPresent()) {
                return null;
            }
            ns = moduleOp.get().getMainModule().getNamespace().getUri();
        }

        return new QName(ns, fName.getLocalName());
    }

    /**
     * Convert YANG value to protobuf value.
     * 
     * @param value the YANG value
     * @param type the YANG type
     * @return protobuf value object
     */
    public static Object convertYangValueToProto(Object value, Object type) {
        if (value == null) {
            return null;
        }
        
        // Use YangProtoTypeMapper for proper type conversion
        if (type instanceof org.yangcentral.yangkit.model.api.stmt.Type) {
            return YangProtoTypeMapper.convertToProtoValue(value, (org.yangcentral.yangkit.model.api.stmt.Type) type);
        }
        
        // Fallback to simple conversion if type is not provided
        return YangProtoTypeMapper.convertToProtoValue(value, null);
    }

    /**
     * Convert protobuf value to YANG value.
     * 
     * @param value the protobuf value
     * @param type the YANG type
     * @return YANG value object
     */
    public static Object convertProtoValueToYang(Object value, Object type) {
        if (value == null) {
            return null;
        }
        
        // Use YangProtoTypeMapper for proper type conversion
        if (type instanceof org.yangcentral.yangkit.model.api.stmt.Type) {
            return YangProtoTypeMapper.convertToYangValue(value, (org.yangcentral.yangkit.model.api.stmt.Type) type);
        }
        
        // Fallback to simple conversion if type is not provided
        return YangProtoTypeMapper.convertToYangValue(value, null);
    }

    /**
     * Get field descriptor from message by field name.
     * 
     * @param message the protobuf message
     * @param fieldName the field name
     * @return field descriptor or null if not found
     */
    public static Descriptors.FieldDescriptor getFieldDescriptor(Message message, String fieldName) {
        if (message == null || fieldName == null) {
            return null;
        }
        return message.getDescriptorForType().findFieldByName(fieldName);
    }

    /**
     * Check if a field is repeated in the protobuf message.
     * 
     * @param message the protobuf message
     * @param fieldName the field name
     * @return true if the field is repeated, false otherwise
     */
    public static boolean isRepeatedField(Message message, String fieldName) {
        Descriptors.FieldDescriptor field = getFieldDescriptor(message, fieldName);
        return field != null && field.isRepeated();
    }
}
