package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.util.Set;

/**
 * Defines a YANG-to-Protobuf mapping convention.
 *
 * <p>A convention encapsulates all rules for converting YANG schema nodes and
 * data values to/from Protobuf format: type mapping, field numbering, value
 * encoding, and structural decisions (wrapper messages, oneof, enum, etc.).
 *
 * <p>Built-in conventions:
 * <ul>
 *   <li>{@code "ygot"} — YGOT-compatible (wrapper messages, FNV-1a field numbers,
 *       bits→enum, union→oneof)</li>
 *   <li>{@code "simple"} — primitive proto types, sequential field numbers</li>
 *   <li>{@code "envelope"} — wraps inner JSON or XML payload in a single
 *       {@code bytes}/{@code string} proto field</li>
 * </ul>
 *
 * <p>Custom conventions can be implemented and registered via
 * {@link YangProtoConventionRegistry#register(YangProtoConvention)}.
 */
public interface YangProtoConvention {

    // ── Identity ──────────────────────────────────────────────────────────────

    /** Unique convention identifier, e.g. {@code "ygot"}, {@code "simple"}, {@code "envelope"}. */
    String getName();

    /** Human-readable description. */
    default String getDescription() { return getName(); }

    // ── Encoding mode ─────────────────────────────────────────────────────────

    /**
     * Whether this convention uses the "envelope" mode, where the entire YANG
     * data payload is serialized to JSON or XML and stored in a single proto
     * field rather than mapping each YANG node to a proto field.
     */
    default boolean isEnvelope() { return false; }

    /**
     * For envelope mode: the format of the inner payload ({@code "json"} or {@code "xml"}).
     * Returns {@code null} for non-envelope conventions.
     */
    default String getEnvelopeInnerFormat() { return null; }

    /**
     * For envelope mode: the name of the proto field that holds the payload.
     * Default is {@code "data"}.
     */
    default String getEnvelopeFieldName() { return "data"; }

    /**
     * For envelope mode: the proto type of the payload field
     * ({@link DescriptorProtos.FieldDescriptorProto.Type#TYPE_BYTES} or
     * {@link DescriptorProtos.FieldDescriptorProto.Type#TYPE_STRING}).
     */
    default DescriptorProtos.FieldDescriptorProto.Type getEnvelopeFieldType() {
        return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
    }

    // ── Type mapping ──────────────────────────────────────────────────────────

    /**
     * Maps a YANG {@link Type} to a proto field type for scalar leaf nodes.
     *
     * @param yangType the YANG type; never {@code null}
     * @return proto field type
     */
    DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(Type yangType);

    /**
     * Returns the fully-qualified wrapper message type name for a YANG type,
     * or {@code null} if this convention does not use wrapper messages for that type.
     *
     * <p>Example (YGOT): {@code ".ywrapper.StringValue"} for {@code string} type.
     *
     * @param yangType the YANG type
     * @return wrapper type name, or {@code null}
     */
    default String getWrapperTypeName(Type yangType) { return null; }

    // ── Structural flags ──────────────────────────────────────────────────────

    /** Whether YANG {@code bits} type should be encoded as a proto enum. */
    default boolean encodeBitsAsEnum() { return false; }

    /** Whether YANG {@code union} type should be encoded as a proto {@code oneof}. */
    default boolean encodeUnionAsOneof() { return false; }

    // ── Field numbering ───────────────────────────────────────────────────────

    /**
     * Computes the next proto field number for the given schema path.
     *
     * @param fieldPath  canonical schema path used as the basis for hash-based numbering
     * @param usedNumbers set of field numbers already used in the current message
     * @return a unique field number in the range [1, 536870911] ∖ [19000, 19999]
     */
    int nextFieldNumber(String fieldPath, Set<Integer> usedNumbers);

    // ── Value conversion ──────────────────────────────────────────────────────

    /**
     * Converts a YANG data value to the appropriate proto runtime value.
     *
     * @param yangValue  the value from {@link org.yangcentral.yangkit.data.api.model.TypedData}
     *                   (may be a String, Integer, Long, Boolean, QName, etc.)
     * @param yangType   the YANG type descriptor
     * @return proto-compatible value
     */
    Object toProtoValue(Object yangValue, Type yangType);

    /**
     * Converts a proto runtime value back to a YANG-compatible string representation
     * suitable for {@link org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory}.
     *
     * @param protoValue the proto value (Integer, Long, Boolean, String, ByteString, etc.)
     * @param yangType   the target YANG type
     * @return string value for the YANG data builder
     */
    Object toYangValue(Object protoValue, Type yangType);

    // ── Proto file dependencies ───────────────────────────────────────────────

    /**
     * Returns the proto file descriptors that the generated descriptor depends on.
     * YGOT mode returns the ywrapper file descriptor; simple and envelope return empty.
     */
    default Descriptors.FileDescriptor[] getDependencies() {
        return new Descriptors.FileDescriptor[0];
    }

    /**
     * Returns proto file paths to declare as {@code import} in the generated
     * {@code .proto} file (e.g. {@code "github.com/.../ywrapper.proto"}).
     */
    default String[] getDependencyImports() {
        return new String[0];
    }
}

