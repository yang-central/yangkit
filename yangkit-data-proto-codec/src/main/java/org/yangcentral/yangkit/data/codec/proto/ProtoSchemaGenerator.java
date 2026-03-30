package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;
import org.yangcentral.yangkit.model.api.stmt.type.Bit;
import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates a protobuf {@link DescriptorProtos.FileDescriptorProto} from a
 * YANG module.
 *
 * <p>Two generation modes are supported:
 * <ul>
 *   <li>{@link ProtoCodecMode#SIMPLE} — primitive protobuf types, sequential
 *       field numbers.</li>
 *   <li>{@link ProtoCodecMode#YGOT} — {@code ywrapper.*Value} wrapper
 *       message types, FNV-1a hash-based field numbers, {@code oneof} for
 *       YANG {@code union}, {@code enum} for bits/enumeration.</li>
 * </ul>
 *
 * <h3>Key design decisions</h3>
 * <ul>
 *   <li>Each YANG container, list, notification or structure becomes a
 *       top-level protobuf message.</li>
 *   <li>Nested containers / lists become nested messages inside the
 *       parent message.</li>
 *   <li>YANG leaf-list → protobuf repeated field.</li>
 *   <li>YANG list → protobuf repeated message field.</li>
 *   <li>YANG choice/case → fields from all cases are flattened into the
 *       parent message (ygot convention).</li>
 *   <li>YANG enumeration → inline {@code EnumDescriptorProto} inside the
 *       containing message.</li>
 *   <li>YANG bits (YGOT mode) → inline {@code EnumDescriptorProto} with bit
 *       positions as values.</li>
 *   <li>YANG union (YGOT mode) → protobuf {@code oneof} block.</li>
 *   <li>YANG leafref → resolved to the target leaf's type.</li>
 * </ul>
 */
public class ProtoSchemaGenerator {

    static final String PACKAGE_PREFIX = "yangkit.proto.";

    private final ProtoCodecMode mode;

    // Per-generation-run state (reset on each generateFileDescriptor call)
    /** Tracks generated message names to avoid duplicates within a file. */
    private final Set<String> generatedMessages = new HashSet<>();

    public ProtoSchemaGenerator(ProtoCodecMode mode) {
        this.mode = mode;
    }

    /** Convenience constructor — uses SIMPLE mode. */
    public ProtoSchemaGenerator() {
        this(ProtoCodecMode.SIMPLE);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Generates a {@link DescriptorProtos.FileDescriptorProto} for the given
     * YANG module.
     *
     * @param module the YANG module; may be {@code null} (returns {@code null})
     * @return file descriptor proto, or {@code null}
     */
    public DescriptorProtos.FileDescriptorProto generateFileDescriptor(Module module) {
        if (module == null) return null;

        generatedMessages.clear();

        String pkg = packageName(module);
        DescriptorProtos.FileDescriptorProto.Builder file =
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setName(pkg.replace('.', '/') + ".proto")
                        .setPackage(pkg)
                        .setSyntax("proto3");

        // Declare ywrapper dependency for YGOT mode
        if (mode == ProtoCodecMode.YGOT) {
            file.addDependency(WrapperTypeManager.YWRAPPER_FILE);
        }

        // Generate messages for all data nodes in the module
        List<DataDefinition> defs = safeGetDataDefs(module);
        if (defs != null) {
            for (DataDefinition def : defs) {
                if (def instanceof DataNode) {
                    DescriptorProtos.DescriptorProto msg = generateMessage((DataNode) def, pkg);
                    if (msg != null) {
                        file.addMessageType(msg);
                    }
                }
            }
        }

        return file.build();
    }

    /**
     * Generates a {@link DescriptorProtos.DescriptorProto} for a YANG data node.
     * Returns {@code null} for leaf nodes (they become fields, not messages).
     */
    public DescriptorProtos.DescriptorProto generateMessage(DataNode dataNode, String parentPath) {
        if (dataNode == null) return null;

        String msgName = messageName(dataNode);
        String path    = parentPath + "/" + dataNode.getIdentifier().getLocalName();

        DescriptorProtos.DescriptorProto.Builder msg =
                DescriptorProtos.DescriptorProto.newBuilder().setName(msgName);

        FieldContext ctx = new FieldContext(path);

        if (dataNode instanceof Container) {
            addContainerFields(msg, (Container) dataNode, ctx);
        } else if (dataNode instanceof YangList) {
            addListFields(msg, (YangList) dataNode, ctx);
        } else if (dataNode instanceof Leaf || dataNode instanceof LeafList) {
            // Pure leaf — no top-level message
            return null;
        } else if (dataNode instanceof Notification) {
            addDataDefContainerFields(msg, (DataDefContainer) dataNode, ctx);
        } else if (dataNode instanceof Rpc) {
            addRpcMessages(msg, (Rpc) dataNode, path);
        } else if (dataNode instanceof YangStructure) {
            addDataDefContainerFields(msg, (DataDefContainer) dataNode, ctx);
        } else {
            return null;
        }

        return msg.build();
    }

    // =========================================================================
    // Field building
    // =========================================================================

    /** Adds fields for a container node to the message builder. */
    private void addContainerFields(DescriptorProtos.DescriptorProto.Builder msg,
                                    Container container, FieldContext ctx) {
        List<DataDefinition> children = safeGetDataDefs(container);
        if (children == null) return;
        for (DataDefinition child : children) {
            addDataDefinition(msg, child, ctx);
        }
    }

    /** Adds fields from any DataDefContainer (notification, structure, etc.). */
    private void addDataDefContainerFields(DescriptorProtos.DescriptorProto.Builder msg,
                                           DataDefContainer container, FieldContext ctx) {
        List<DataDefinition> children = safeGetDataDefs(container);
        if (children == null) return;
        for (DataDefinition child : children) {
            addDataDefinition(msg, child, ctx);
        }
    }

    /**
     * Adds fields for a list node.
     * Key fields are added first; then the remaining non-key leaf/container fields.
     * Duplicate field names (a key field also appearing in the full child list)
     * are suppressed.
     */
    private void addListFields(DescriptorProtos.DescriptorProto.Builder msg,
                               YangList list, FieldContext ctx) {
        Set<String> addedFields = new HashSet<>();

        // 1. Key fields first
        Key key = list.getKey();
        if (key != null) {
            try {
                List<Leaf> keyLeaves = key.getkeyNodes();
                if (keyLeaves != null) {
                    for (Leaf kl : keyLeaves) {
                        String snakeName = toSnakeCase(kl.getIdentifier().getLocalName());
                        if (addedFields.add(snakeName)) {
                            addLeafField(msg, kl, ctx);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. All other children (skip already-added key fields)
        List<DataDefinition> children = safeGetDataDefs(list);
        if (children != null) {
            for (DataDefinition child : children) {
                String snakeName = childSnakeName(child);
                if (snakeName != null && addedFields.add(snakeName)) {
                    addDataDefinition(msg, child, ctx);
                } else if (snakeName == null) {
                    // composite children (Choice etc.) — no single name, just add
                    addDataDefinition(msg, child, ctx);
                }
            }
        }
    }

    /** Dispatches a single DataDefinition to the appropriate field adder. */
    private void addDataDefinition(DescriptorProtos.DescriptorProto.Builder msg,
                                   DataDefinition child, FieldContext ctx) {
        if (child instanceof Leaf) {
            addLeafField(msg, (Leaf) child, ctx);
        } else if (child instanceof LeafList) {
            addLeafListField(msg, (LeafList) child, ctx);
        } else if (child instanceof Container) {
            addNestedMessageField(msg, (Container) child, ctx, false);
        } else if (child instanceof YangList) {
            addNestedMessageField(msg, (YangList) child, ctx, true);
        } else if (child instanceof Choice) {
            // Flatten all cases into parent message (ygot convention)
            flattenChoice(msg, (Choice) child, ctx);
        }
        // Anydata / Anyxml are handled via their own codec — skip in schema
    }

    // -----------------------------------------------------------------------
    // Leaf field
    // -----------------------------------------------------------------------

    private void addLeafField(DescriptorProtos.DescriptorProto.Builder msg,
                              Leaf leaf, FieldContext ctx) {
        if (leaf == null) return;

        String fieldName = toSnakeCase(leaf.getIdentifier().getLocalName());
        String fieldPath = ctx.path + "/" + leaf.getIdentifier().getLocalName();
        int    fieldNum  = ctx.nextFieldNumber(fieldPath);
        Type   type      = leaf.getType();

        addTypedField(msg, fieldName, fieldNum, type, fieldPath, false, ctx);
    }

    private void addLeafListField(DescriptorProtos.DescriptorProto.Builder msg,
                                  LeafList leafList, FieldContext ctx) {
        if (leafList == null) return;

        String fieldName = toSnakeCase(leafList.getIdentifier().getLocalName());
        String fieldPath = ctx.path + "/" + leafList.getIdentifier().getLocalName();
        int    fieldNum  = ctx.nextFieldNumber(fieldPath);
        Type   type      = leafList.getType();

        addTypedField(msg, fieldName, fieldNum, type, fieldPath, true, ctx);
    }

    /**
     * Core method that builds a field descriptor for a typed (leaf / leaf-list) node.
     * Handles all YANG types, including enum descriptor generation and union oneof.
     */
    private void addTypedField(DescriptorProtos.DescriptorProto.Builder msg,
                               String fieldName, int fieldNum, Type type,
                               String fieldPath, boolean repeated, FieldContext ctx) {
        if (type == null) {
            // Fallback: plain string
            msg.addField(primitiveField(fieldName, fieldNum, repeated,
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
            return;
        }

        Restriction<?> restriction = type.getRestriction();
        if (restriction == null) {
            msg.addField(primitiveField(fieldName, fieldNum, repeated,
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
            return;
        }

        String typeName = YangProtoTypeMapper.getBaseTypeName(restriction);

        switch (typeName) {
            case "enumeration": {
                // Generate inline enum descriptor and reference it
                String enumName = toPascalCase(fieldName) + "Enum";
                DescriptorProtos.EnumDescriptorProto enumProto =
                        buildEnumDescriptor(enumName, (Enumeration) restriction);
                msg.addEnumType(enumProto);
                // Field references the enum by type_name
                msg.addField(enumField(fieldName, fieldNum, repeated, enumName));
                break;
            }
            case "bits": {
                if (mode == ProtoCodecMode.YGOT) {
                    // Bits → proto enum with bit-position values
                    String enumName = toPascalCase(fieldName) + "Bits";
                    DescriptorProtos.EnumDescriptorProto bitsProto =
                            buildBitsEnumDescriptor(enumName, (Bits) restriction);
                    msg.addEnumType(bitsProto);
                    msg.addField(enumField(fieldName, fieldNum, repeated, enumName));
                } else {
                    // SIMPLE: store as string
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
                }
                break;
            }
            case "union": {
                if (mode == ProtoCodecMode.YGOT && !repeated) {
                    // Union → oneof with one field per member type
                    addUnionOneof(msg, fieldName, fieldNum, (Union) restriction, fieldPath, ctx);
                } else {
                    // SIMPLE or repeated union: store as string
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
                }
                break;
            }
            case "leafref": {
                // Resolve to the referenced node's type
                Type resolvedType = resolveLeafRef(type);
                if (resolvedType != null && resolvedType != type) {
                    // Re-enter with the resolved type
                    addTypedField(msg, fieldName, fieldNum, resolvedType, fieldPath, repeated, ctx);
                } else {
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
                }
                break;
            }
            case "identityref": {
                if (mode == ProtoCodecMode.YGOT) {
                    // YGOT: string wrapper (identities are referenced by their module-prefixed name)
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                            YangProtoTypeMapper.getYwrapperTypeName(type)));
                } else {
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
                }
                break;
            }
            default: {
                // Scalar types
                if (mode == ProtoCodecMode.YGOT) {
                    String wrapperTypeName = YangProtoTypeMapper.getYwrapperTypeName(type);
                    if (wrapperTypeName != null) {
                        msg.addField(primitiveField(fieldName, fieldNum, repeated,
                                DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                                wrapperTypeName));
                    } else {
                        msg.addField(primitiveField(fieldName, fieldNum, repeated,
                                DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
                    }
                } else {
                    msg.addField(primitiveField(fieldName, fieldNum, repeated,
                            YangProtoTypeMapper.getProtoFieldType(type, mode), null));
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Nested message (container / list) field
    // -----------------------------------------------------------------------

    private void addNestedMessageField(DescriptorProtos.DescriptorProto.Builder parent,
                                       DataNode child, FieldContext ctx, boolean repeated) {
        if (child == null) return;

        // Generate the nested message
        DescriptorProtos.DescriptorProto nested = generateMessage(child, ctx.path);
        if (nested != null) {
            parent.addNestedType(nested);
        }

        String fieldName = toSnakeCase(child.getIdentifier().getLocalName());
        String fieldPath = ctx.path + "/" + child.getIdentifier().getLocalName();
        int    fieldNum  = ctx.nextFieldNumber(fieldPath);
        String typeName  = messageName(child); // relative name for nested types

        DescriptorProtos.FieldDescriptorProto.Builder field =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(fieldName)
                        .setNumber(fieldNum)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                        .setTypeName(typeName)
                        .setLabel(repeated
                                ? DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
                                : DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        parent.addField(field);
    }

    // -----------------------------------------------------------------------
    // Choice/Case flattening
    // -----------------------------------------------------------------------

    /** Flattens all cases of a YANG choice into the parent message fields. */
    private void flattenChoice(DescriptorProtos.DescriptorProto.Builder msg,
                               Choice choice, FieldContext ctx) {
        try {
            List<Case> cases = choice.getCases();
            if (cases == null) return;
            for (Case c : cases) {
                List<DataDefinition> caseDefs = safeGetDataDefs(c);
                if (caseDefs != null) {
                    for (DataDefinition def : caseDefs) {
                        addDataDefinition(msg, def, ctx);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------------------------------
    // RPC
    // -----------------------------------------------------------------------

    /**
     * Adds nested Input/Output messages for an RPC.
     * The RPC message itself is largely empty; its content is in the nested types.
     */
    private void addRpcMessages(DescriptorProtos.DescriptorProto.Builder rpcMsg,
                                Rpc rpc, String parentPath) {
        String rpcPascal = toPascalCase(rpc.getIdentifier().getLocalName());

        Input input = rpc.getInput();
        if (input != null) {
            String inputMsgName = rpcPascal + "Input";
            DescriptorProtos.DescriptorProto inputMsg =
                    buildInputOutputMessage(inputMsgName, input, parentPath);
            if (inputMsg != null) rpcMsg.addNestedType(inputMsg);
        }

        Output output = rpc.getOutput();
        if (output != null) {
            String outputMsgName = rpcPascal + "Output";
            DescriptorProtos.DescriptorProto outputMsg =
                    buildInputOutputMessage(outputMsgName, output, parentPath);
            if (outputMsg != null) rpcMsg.addNestedType(outputMsg);
        }
    }

    private DescriptorProtos.DescriptorProto buildInputOutputMessage(
            String msgName, DataDefContainer container, String parentPath) {
        if (container == null) return null;
        DescriptorProtos.DescriptorProto.Builder msg =
                DescriptorProtos.DescriptorProto.newBuilder().setName(msgName);
        FieldContext ctx = new FieldContext(parentPath + "/" + msgName);
        addDataDefContainerFields(msg, container, ctx);
        return msg.build();
    }

    // -----------------------------------------------------------------------
    // Enum descriptors
    // -----------------------------------------------------------------------

    /** Builds an EnumDescriptorProto for a YANG enumeration restriction. */
    private DescriptorProtos.EnumDescriptorProto buildEnumDescriptor(
            String enumName, Enumeration enumRestriction) {
        DescriptorProtos.EnumDescriptorProto.Builder proto =
                DescriptorProtos.EnumDescriptorProto.newBuilder().setName(enumName);

        // proto3 requires a zero value
        boolean hasZero = false;

        try {
            List<YangEnum> enums = enumRestriction.getEffectiveEnums();
            if (enums != null) {
                for (YangEnum e : enums) {
                    String valueName = toUpperSnakeCase(e.getArgStr());
                    Integer intVal   = enumRestriction.getEnumActualValue(e.getArgStr());
                    int     num      = intVal != null ? intVal : 0;
                    if (num == 0) hasZero = true;
                    proto.addValue(DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                            .setName(valueName)
                            .setNumber(num));
                }
            }
        } catch (Exception ignored) {}

        if (!hasZero) {
            // Ensure there is a zero value (proto3 requirement)
            proto.addValue(0, DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                    .setName(enumName.toUpperCase() + "_UNSPECIFIED")
                    .setNumber(0));
        }

        return proto.build();
    }

    /** Builds an EnumDescriptorProto for a YANG bits type (YGOT mode). */
    private DescriptorProtos.EnumDescriptorProto buildBitsEnumDescriptor(
            String enumName, Bits bitsRestriction) {
        DescriptorProtos.EnumDescriptorProto.Builder proto =
                DescriptorProtos.EnumDescriptorProto.newBuilder().setName(enumName);

        boolean hasZero = false;
        try {
            List<Bit> bits = bitsRestriction.getEffectiveBits();
            if (bits != null) {
                for (Bit b : bits) {
                    String valueName = toUpperSnakeCase(b.getArgStr());
                    long   pos       = bitsRestriction.getBitActualPosition(b.getArgStr());
                    int    intPos    = (int) pos;
                    if (intPos == 0) hasZero = true;
                    proto.addValue(DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                            .setName(valueName)
                            .setNumber(intPos));
                }
            }
        } catch (Exception ignored) {}

        if (!hasZero) {
            proto.addValue(0, DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                    .setName(enumName.toUpperCase() + "_UNSET")
                    .setNumber(0));
        }

        return proto.build();
    }

    // -----------------------------------------------------------------------
    // Union → oneof (YGOT mode)
    // -----------------------------------------------------------------------

    /**
     * Adds a protobuf {@code oneof} block for a YANG union type (YGOT mode).
     * Each union member gets one field inside the oneof.
     */
    private void addUnionOneof(DescriptorProtos.DescriptorProto.Builder msg,
                               String fieldName, int baseFieldNum,
                               Union unionRestriction, String fieldPath, FieldContext ctx) {
        DescriptorProtos.OneofDescriptorProto.Builder oneof =
                DescriptorProtos.OneofDescriptorProto.newBuilder()
                        .setName(fieldName + "_oneof");

        int oneofIndex = msg.getOneofDeclCount();
        msg.addOneofDecl(oneof);

        try {
            List<Type> memberTypes = unionRestriction.getActualTypes();
            if (memberTypes != null) {
                int memberIdx = 0;
                for (Type memberType : memberTypes) {
                    String memberTypeName = YangProtoTypeMapper.getBaseTypeName(
                            memberType.getRestriction());
                    String memberFieldName = fieldName + "_" + memberTypeName
                            + (memberIdx > 0 ? "_" + memberIdx : "");
                    String memberFieldPath = fieldPath + "_" + memberTypeName + "_" + memberIdx;
                    int memberFieldNum = ctx.nextFieldNumber(memberFieldPath);

                    String wrapperTypeName = YangProtoTypeMapper.getYwrapperTypeName(memberType);
                    DescriptorProtos.FieldDescriptorProto.Builder memberField;
                    if (wrapperTypeName != null) {
                        memberField = DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName(memberFieldName)
                                .setNumber(memberFieldNum)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                                .setTypeName(wrapperTypeName)
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setOneofIndex(oneofIndex);
                    } else {
                        memberField = DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName(memberFieldName)
                                .setNumber(memberFieldNum)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setOneofIndex(oneofIndex);
                    }
                    msg.addField(memberField);
                    memberIdx++;
                }
            }
        } catch (Exception ignored) {
            // If union member resolution fails, fall back to a plain string field
            msg.addField(primitiveField(fieldName, baseFieldNum, false,
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, null));
        }
    }

    // -----------------------------------------------------------------------
    // Leafref resolution
    // -----------------------------------------------------------------------

    private Type resolveLeafRef(Type type) {
        if (type == null) return null;
        Restriction<?> r = type.getRestriction();
        if (!(r instanceof LeafRef)) return type;
        try {
            TypedDataNode referenced = ((LeafRef) r).getReferencedNode();
            if (referenced != null) {
                Type refType = referenced.getType();
                if (refType != null && !(refType.getRestriction() instanceof LeafRef)) {
                    return refType;
                }
                // Transitive leafref
                return resolveLeafRef(refType);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // -----------------------------------------------------------------------
    // Field descriptor helpers
    // -----------------------------------------------------------------------

    private DescriptorProtos.FieldDescriptorProto primitiveField(
            String name, int number, boolean repeated,
            DescriptorProtos.FieldDescriptorProto.Type type,
            String typeName) {
        DescriptorProtos.FieldDescriptorProto.Builder f =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(name)
                        .setNumber(number)
                        .setType(type)
                        .setLabel(repeated
                                ? DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
                                : DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        if (typeName != null) f.setTypeName(typeName);
        return f.build();
    }

    private DescriptorProtos.FieldDescriptorProto enumField(
            String name, int number, boolean repeated, String enumName) {
        return DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(name)
                .setNumber(number)
                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM)
                .setTypeName(enumName)
                .setLabel(repeated
                        ? DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
                        : DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                .build();
    }

    // =========================================================================
    // Naming helpers
    // =========================================================================

    /** Returns the protobuf package name for a YANG module. */
    public String packageName(Module module) {
        if (module == null) return PACKAGE_PREFIX + "unknown";
        try {
            String name = module.getModuleId().getModuleName();
            if (name != null) {
                return PACKAGE_PREFIX + name.toLowerCase().replace('-', '_');
            }
        } catch (Exception ignored) {}
        return PACKAGE_PREFIX + "unknown";
    }

    /** Returns the PascalCase message name for a DataNode. */
    public static String messageName(DataNode node) {
        if (node == null) return "Unknown";
        return toPascalCase(node.getIdentifier().getLocalName());
    }

    /** Converts a YANG identifier (hyphenated) to PascalCase. */
    public static String toPascalCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : s.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                cap = true;
            } else if (cap) {
                sb.append(Character.toUpperCase(c));
                cap = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a YANG identifier (hyphenated or camelCase) to snake_case.
     * Hyphens become underscores; uppercase letters get a preceding underscore.
     */
    public static String toSnakeCase(String s) {
        if (s == null || s.isEmpty()) return s;
        // First replace hyphens
        String h = s.replace('-', '_');
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.length(); i++) {
            char c = h.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && h.charAt(i - 1) != '_') sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Converts a string to UPPER_SNAKE_CASE for enum value names. */
    private static String toUpperSnakeCase(String s) {
        return toSnakeCase(s).toUpperCase();
    }

    // =========================================================================
    // Field numbering
    // =========================================================================

    /**
     * Encapsulates per-message field numbering state.
     * In SIMPLE mode: sequential (1, 2, 3 …).
     * In YGOT mode: FNV-1a hash of schema path, with collision resolution.
     */
    private class FieldContext {
        final String path;
        private int seq = 1;
        private final Set<Integer> usedNumbers = new HashSet<>();

        FieldContext(String path) { this.path = path; }

        int nextFieldNumber(String fieldPath) {
            if (mode == ProtoCodecMode.YGOT) {
                return fnvFieldNumber(fieldPath, usedNumbers);
            } else {
                int n = seq++;
                usedNumbers.add(n);
                return n;
            }
        }
    }

    /**
     * Computes a stable field number using FNV-1a hash of the schema path,
     * as used by ygot's proto generator.  Handles collisions by incrementing.
     */
    private static int fnvFieldNumber(String path, Set<Integer> usedNumbers) {
        // FNV-1a 32-bit
        long hash = 2166136261L;
        for (byte b : path.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xFF);
            hash  = (hash * 16777619L) & 0xFFFFFFFFL;
        }
        int num = (int) (hash % 536870911L) + 1; // 1 … 536870911
        if (num < 1) num = 1;
        // Avoid proto reserved range 19000-19999
        if (num >= 19000 && num <= 19999) num = 20000;
        // Collision resolution
        while (usedNumbers.contains(num)) num++;
        usedNumbers.add(num);
        return num;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /** Returns the snake_case name of a DataDefinition child, or null for composite nodes. */
    private static String childSnakeName(DataDefinition child) {
        if (child instanceof SchemaNode) {
            return toSnakeCase(((SchemaNode) child).getIdentifier().getLocalName());
        }
        return null;
    }

    private static List<DataDefinition> safeGetDataDefs(DataDefContainer c) {
        if (c == null) return null;
        try {
            return c.getDataDefChildren();
        } catch (Exception e) {
            return null;
        }
    }
}
