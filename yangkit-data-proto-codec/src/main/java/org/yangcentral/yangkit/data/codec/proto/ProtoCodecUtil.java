package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Utility methods used by all proto codec classes for
 * serialization and deserialization of YANG data.
 *
 * <h3>Key responsibilities</h3>
 * <ul>
 *   <li>Serialize a {@link YangDataContainer}'s children into a protobuf
 *       {@link Message.Builder}.</li>
 *   <li>Deserialize a {@link DynamicMessage}'s fields back into a
 *       {@link YangDataContainer}.</li>
 *   <li>Convert YANG qualified names to/from protobuf snake_case field
 *       names.</li>
 *   <li>Delegate type-level value conversion to
 *       {@link YangProtoTypeMapper}.</li>
 * </ul>
 */
public class ProtoCodecUtil {

    private ProtoCodecUtil() {}

    // =========================================================================
    // Deserialization helpers (proto → YANG)
    // =========================================================================

    /**
     * Reads all fields from {@code message} and adds corresponding
     * {@link YangData} children to {@code container}.
     *
     * @param container           the YANG data container to populate
     * @param message             the protobuf message to read from
     * @param validatorResultBuilder accumulates validation errors
     */
    public static void deserializeChildren(YangDataContainer container,
                                           DynamicMessage message,
                                           ValidatorResultBuilder validatorResultBuilder) {
        deserializeChildren(container, message, validatorResultBuilder, ProtoCodecMode.SIMPLE, null, null);
    }

    public static void deserializeChildren(YangDataContainer container,
                                           DynamicMessage message,
                                           ValidatorResultBuilder validatorResultBuilder,
                                           ProtoCodecMode mode,
                                           AnydataValidationContextResolver resolver,
                                           String sourcePath) {
        if (message == null) return;

        for (Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
            if (!message.hasField(field) && !field.isRepeated()) continue;

            String protoFieldName = field.getName(); // already snake_case

            // Find the matching YANG schema node
            SchemaNode schemaNode = findSchemaNodeByProtoName(container, protoFieldName);
            if (schemaNode == null) continue;

            if (field.isRepeated()) {
                int count = message.getRepeatedFieldCount(field);
                for (int i = 0; i < count; i++) {
                    Object fieldValue = message.getRepeatedField(field, i);
                    createAndAdd(container, field, fieldValue, schemaNode, validatorResultBuilder,
                            mode, resolver, buildChildSourcePath(sourcePath, schemaNode, i));
                }
            } else {
                Object fieldValue = message.getField(field);
                createAndAdd(container, field, fieldValue, schemaNode, validatorResultBuilder,
                        mode, resolver, buildChildSourcePath(sourcePath, schemaNode, null));
            }
        }
    }

    private static void createAndAdd(YangDataContainer container,
                                     Descriptors.FieldDescriptor field,
                                     Object fieldValue,
                                     SchemaNode schemaNode,
                                     ValidatorResultBuilder validatorResultBuilder,
                                     ProtoCodecMode mode,
                                     AnydataValidationContextResolver resolver,
                                     String sourcePath) {
        if (fieldValue == null) return;

        YangData<?> yangData = null;

        if (schemaNode instanceof Leaf) {
            Leaf leaf = (Leaf) schemaNode;
            Object yangValue = YangProtoTypeMapper.convertToYangValue(fieldValue, leaf.getType());
            yangData = createLeafData(leaf, yangValue);

        } else if (schemaNode instanceof LeafList) {
            LeafList ll = (LeafList) schemaNode;
            Object yangValue = YangProtoTypeMapper.convertToYangValue(fieldValue, ll.getType());
            yangData = createLeafListData(ll, yangValue);

        } else if (schemaNode instanceof Container) {
            if (fieldValue instanceof DynamicMessage) {
                yangData = createContainerData((Container) schemaNode,
                        (DynamicMessage) fieldValue, validatorResultBuilder, mode, resolver, sourcePath);
            }

        } else if (schemaNode instanceof YangList) {
            if (fieldValue instanceof DynamicMessage) {
                yangData = createListData((YangList) schemaNode,
                        (DynamicMessage) fieldValue, validatorResultBuilder, mode, resolver, sourcePath);
            }

        } else if (schemaNode instanceof Anydata) {
            if (fieldValue instanceof DynamicMessage) {
                AnyDataDataProtoCodec codec = (AnyDataDataProtoCodec) YangDataProtoCodec.getInstance(
                        schemaNode, mode, resolver, sourcePath);
                yangData = codec.deserialize((DynamicMessage) fieldValue, validatorResultBuilder);
            }

        } else if (schemaNode instanceof Anyxml) {
            yangData = createAnyXmlData((Anyxml) schemaNode, fieldValue);
        }

        if (yangData != null) {
            try {
                container.addDataChild(yangData);
            } catch (Exception e) {
                System.err.println("[ProtoCodecUtil] Failed to add child "
                        + schemaNode.getIdentifier() + ": " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // Serialization helpers (YANG → proto)
    // =========================================================================

    /**
     * Serializes the children of a {@link YangDataContainer} into
     * {@code messageBuilder}.
     *
     * @param messageBuilder     the proto message builder to populate
     * @param yangDataContainer  the YANG data container to read from
     */
    public static void serializeChildren(Message.Builder messageBuilder,
                                         YangDataContainer yangDataContainer,
                                         ProtoCodecMode mode) {
        List<YangData<?>> children = yangDataContainer.getDataChildren();
        if (children == null) return;

        for (YangData<?> child : children) {
            if (child == null || child.isDummyNode()) continue;

            SchemaNode schema = child.getSchemaNode();
            String fieldName  = toSnakeCase(child.getQName().getLocalName());

            Descriptors.FieldDescriptor fieldDesc =
                    messageBuilder.getDescriptorForType().findFieldByName(fieldName);
            if (fieldDesc == null) continue;

            if (schema instanceof Leaf) {
                setLeafField(messageBuilder, fieldDesc, (LeafData<?>) child);

            } else if (schema instanceof LeafList) {
                setLeafListField(messageBuilder, fieldDesc, (LeafData<?>) child);

            } else if (schema instanceof Container || schema instanceof YangList) {
                // Recurse into child codec
                DynamicMessage childMsg = YangDataProtoCodec
                        .getInstance(schema, mode)
                        .serialize(child);
                if (childMsg != null) {
                    if (fieldDesc.isRepeated()) {
                        messageBuilder.addRepeatedField(fieldDesc, childMsg);
                    } else {
                        messageBuilder.setField(fieldDesc, childMsg);
                    }
                }
            } else if (schema instanceof Anydata) {
                DynamicMessage childMsg = YangDataProtoCodec
                        .getInstance(schema, mode)
                        .serialize(child);
                if (childMsg != null) {
                    if (fieldDesc.isRepeated()) {
                        messageBuilder.addRepeatedField(fieldDesc, childMsg);
                    } else {
                        messageBuilder.setField(fieldDesc, childMsg);
                    }
                }
            }
        }
    }

    private static void setLeafField(Message.Builder builder,
                                     Descriptors.FieldDescriptor fieldDesc,
                                     LeafData<?> leafData) {
        try {
            String strValue = leafData.getStringValue();
            if (strValue == null) return;

            Leaf leafSchema = (Leaf) leafData.getSchemaNode();
            Object protoValue = YangProtoTypeMapper.convertToProtoValue(strValue, leafSchema.getType());
            if (protoValue == null) return;

            // For YGOT wrapper types, the field type is TYPE_MESSAGE
            // and we need to wrap the value into the wrapper message
            if (fieldDesc.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                DynamicMessage wrapped = wrapScalarValue(protoValue, fieldDesc.getMessageType());
                if (wrapped != null) builder.setField(fieldDesc, wrapped);
            } else {
                builder.setField(fieldDesc, protoValue);
            }
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to set leaf field "
                    + fieldDesc.getName() + ": " + e.getMessage());
        }
    }

    private static void setLeafListField(Message.Builder builder,
                                          Descriptors.FieldDescriptor fieldDesc,
                                          LeafData<?> leafData) {
        try {
            String strValue = leafData.getStringValue();
            if (strValue == null) return;

            LeafList llSchema = (LeafList) leafData.getSchemaNode();
            Object protoValue = YangProtoTypeMapper.convertToProtoValue(strValue, llSchema.getType());
            if (protoValue == null) return;

            if (fieldDesc.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                DynamicMessage wrapped = wrapScalarValue(protoValue, fieldDesc.getMessageType());
                if (wrapped != null) builder.addRepeatedField(fieldDesc, wrapped);
            } else {
                builder.addRepeatedField(fieldDesc, protoValue);
            }
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to add leaf-list field "
                    + fieldDesc.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Wraps a scalar proto value into a ywrapper message (YGOT mode).
     * The wrapper message is expected to have exactly one field named "value".
     */
    private static DynamicMessage wrapScalarValue(Object value, Descriptors.Descriptor wrapperDesc) {
        if (wrapperDesc == null) return null;
        Descriptors.FieldDescriptor valueField = wrapperDesc.findFieldByName("value");
        if (valueField == null) return null;

        // Special handling for Decimal64Value which has two fields (digits, precision)
        if ("Decimal64Value".equals(wrapperDesc.getName())) {
            return null; // Handled separately if needed
        }

        try {
            DynamicMessage.Builder b = DynamicMessage.newBuilder(wrapperDesc);
            b.setField(valueField, coerceValue(value, valueField));
            return b.build();
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to wrap value " + value
                    + " into " + wrapperDesc.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /** Coerces a value to the Java type expected by the protobuf field. */
    private static Object coerceValue(Object value, Descriptors.FieldDescriptor field) {
        switch (field.getType()) {
            case INT32:  case SINT32: case SFIXED32:
                if (value instanceof Number) return ((Number) value).intValue();
                return Integer.parseInt(value.toString());
            case INT64:  case SINT64: case SFIXED64:
                if (value instanceof Number) return ((Number) value).longValue();
                return Long.parseLong(value.toString());
            case UINT32: case FIXED32:
                if (value instanceof Number) return ((Number) value).intValue();
                return Integer.parseUnsignedInt(value.toString());
            case UINT64: case FIXED64:
                if (value instanceof Number) return ((Number) value).longValue();
                return Long.parseUnsignedLong(value.toString());
            case BOOL:
                if (value instanceof Boolean) return value;
                return "true".equalsIgnoreCase(value.toString());
            case BYTES:
                if (value instanceof ByteString) return value;
                if (value instanceof byte[]) return ByteString.copyFrom((byte[]) value);
                return ByteString.copyFromUtf8(value.toString());
            case STRING:
                return value.toString();
            default:
                return value;
        }
    }

    // =========================================================================
    // Schema node lookup
    // =========================================================================

    /**
     * Finds the YANG schema node whose snake_case name matches
     * {@code protoFieldName} within the container's schema children.
     *
     * <p>Handles nested choice/case nodes by recursively searching case
     * contents.
     */
    static SchemaNode findSchemaNodeByProtoName(YangDataContainer container,
                                                String protoFieldName) {
        SchemaNode schema = null;
        if (container instanceof YangData) {
            schema = ((YangData<?>) container).getSchemaNode();
        }
        if (schema instanceof DataDefContainer) {
            return searchInDataDefs((DataDefContainer) schema, protoFieldName);
        }
        return null;
    }

    private static SchemaNode searchInDataDefs(DataDefContainer container,
                                               String protoFieldName) {
        List<DataDefinition> children;
        try {
            children = container.getDataDefChildren();
        } catch (Exception e) {
            return null;
        }
        if (children == null) return null;

        for (DataDefinition child : children) {
            if (!(child instanceof SchemaNode)) continue;
            SchemaNode sn = (SchemaNode) child;

            if (child instanceof Choice) {
                // Flatten choice: search inside all cases
                Choice choice = (Choice) child;
                try {
                    List<Case> cases = choice.getCases();
                    if (cases != null) {
                        for (Case c : cases) {
                            SchemaNode found = searchInDataDefs(c, protoFieldName);
                            if (found != null) return found;
                        }
                    }
                } catch (Exception ignored) {}
                continue;
            }

            String snakeLocalName = toSnakeCase(sn.getIdentifier().getLocalName());
            if (protoFieldName.equals(snakeLocalName)) {
                return sn;
            }
        }
        return null;
    }

    // =========================================================================
    // YangData creation helpers
    // =========================================================================

    static YangData<?> createLeafData(Leaf leaf, Object value) {
        if (value == null) return null;
        try {
            return YangDataBuilderFactory.getBuilder().getYangData(leaf, value.toString());
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to create leaf data for "
                    + leaf.getIdentifier() + ": " + e.getMessage());
            return null;
        }
    }

    static YangData<?> createLeafListData(LeafList leafList, Object value) {
        if (value == null) return null;
        try {
            return YangDataBuilderFactory.getBuilder().getYangData(leafList, value.toString());
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to create leaf-list data for "
                    + leafList.getIdentifier() + ": " + e.getMessage());
            return null;
        }
    }

    private static YangData<?> createContainerData(Container container,
                                                    DynamicMessage message,
                                                    ValidatorResultBuilder validatorResultBuilder,
                                                    ProtoCodecMode mode,
                                                    AnydataValidationContextResolver resolver,
                                                    String sourcePath) {
        try {
            org.yangcentral.yangkit.data.impl.model.ContainerDataImpl data =
                    new org.yangcentral.yangkit.data.impl.model.ContainerDataImpl(container);
            data.setQName(container.getIdentifier());
            deserializeChildren(data, message, validatorResultBuilder, mode, resolver, sourcePath);
            return data;
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to create container data: " + e.getMessage());
            return null;
        }
    }

    private static YangData<?> createListData(YangList list,
                                               DynamicMessage message,
                                               ValidatorResultBuilder validatorResultBuilder,
                                               ProtoCodecMode mode,
                                               AnydataValidationContextResolver resolver,
                                               String sourcePath) {
        try {
            org.yangcentral.yangkit.data.impl.model.ListDataImpl data =
                    new org.yangcentral.yangkit.data.impl.model.ListDataImpl(list, null);
            data.setQName(list.getIdentifier());
            deserializeChildren(data, message, validatorResultBuilder, mode, resolver, sourcePath);
            return data;
        } catch (Exception e) {
            System.err.println("[ProtoCodecUtil] Failed to create list data: " + e.getMessage());
            return null;
        }
    }

    private static YangData<?> createAnyXmlData(Anyxml anyxml, Object fieldValue) {
        try {
            org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl data =
                    new org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl(anyxml);
            data.setQName(anyxml.getIdentifier());
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // Name conversion helpers (public for use by other codec classes)
    // =========================================================================

    /**
     * Returns the protobuf field name for a YANG QName by converting the
     * local name to snake_case.
     */
    public static String getProtoFieldNameFromQName(QName qName,
                                                     YangDataContainer parent) {
        if (qName == null) return null;
        return toSnakeCase(qName.getLocalName());
    }

    /**
     * Reconstructs a YANG {@link QName} from a proto field name (snake_case).
     * Uses the parent container's namespace when no module prefix is present.
     */
    public static QName getQNameFromProtoField(String fieldName,
                                                YangDataContainer parent) {
        if (fieldName == null) return null;
        FName fName = new FName(fieldName);
        URI ns = null;

        String prefix = fName.getPrefix();
        if (prefix == null) {
            // Inherit namespace from parent
            if (parent instanceof YangDataDocument) {
                ns = ((YangDataDocument) parent).getQName().getNamespace();
            } else if (parent instanceof YangData) {
                ns = ((YangData<?>) parent).getQName().getNamespace();
            }
        } else {
            YangSchemaContext schemaContext = null;
            if (parent instanceof YangDataDocument) {
                schemaContext = ((YangDataDocument) parent).getSchemaContext();
            } else if (parent instanceof YangData) {
                schemaContext = ((YangData<?>) parent).getSchemaNode()
                        .getContext().getSchemaContext();
            }
            if (schemaContext != null) {
                Optional<Module> opt = schemaContext.getLatestModule(prefix);
                if (opt.isPresent()) {
                    ns = opt.get().getMainModule().getNamespace().getUri();
                }
            }
        }

        if (ns == null) return null;
        return new QName(ns, fName.getLocalName());
    }

    /**
     * Converts a YANG value to its proto representation.
     *
     * @param value the YANG value
     * @param type  the YANG {@link org.yangcentral.yangkit.model.api.stmt.Type},
     *              or {@code null} for best-effort conversion
     */
    public static Object convertYangValueToProto(Object value, Object type) {
        if (value == null) return null;
        if (type instanceof org.yangcentral.yangkit.model.api.stmt.Type) {
            return YangProtoTypeMapper.convertToProtoValue(
                    value, (org.yangcentral.yangkit.model.api.stmt.Type) type);
        }
        return YangProtoTypeMapper.convertToProtoValue(value, null);
    }

    /**
     * Converts a proto value to its YANG representation.
     *
     * @param value the proto value
     * @param type  the target YANG {@link org.yangcentral.yangkit.model.api.stmt.Type},
     *              or {@code null} for best-effort conversion
     */
    public static Object convertProtoValueToYang(Object value, Object type) {
        if (value == null) return null;
        if (type instanceof org.yangcentral.yangkit.model.api.stmt.Type) {
            return YangProtoTypeMapper.convertToYangValue(
                    value, (org.yangcentral.yangkit.model.api.stmt.Type) type);
        }
        return YangProtoTypeMapper.convertToYangValue(value, null);
    }

    // =========================================================================
    // Internal string utilities
    // =========================================================================

    /** Converts a YANG identifier to protobuf snake_case field name. */
    static String toSnakeCase(String s) {
        return ProtoSchemaGenerator.toSnakeCase(s);
    }

    private static String buildChildSourcePath(String parentSourcePath, SchemaNode schemaNode, Integer index) {
        String localName = schemaNode == null ? null : schemaNode.getIdentifier().getLocalName();
        if (localName == null) {
            return parentSourcePath;
        }
        String path = (parentSourcePath == null || parentSourcePath.isEmpty())
                ? "/" + localName
                : parentSourcePath + "/" + localName;
        if (index != null) {
            path += "[" + index + "]";
        }
        return path;
    }
}
