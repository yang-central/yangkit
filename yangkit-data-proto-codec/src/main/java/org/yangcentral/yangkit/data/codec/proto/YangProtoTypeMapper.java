package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

/**
 * Maps between YANG types and Protobuf field types, and converts values
 * between the two type systems.
 *
 * <p>Supports two modes:
 * <ul>
 *   <li>{@link ProtoCodecMode#YGOT} — scalars use {@code ywrapper.*Value}
 *       wrapper messages (requires the ywrapper file descriptor as a
 *       proto dependency).</li>
 *   <li>{@link ProtoCodecMode#SIMPLE} — scalars use proto3 primitive types
 *       directly ({@code int32}, {@code string}, {@code bool}, …).</li>
 * </ul>
 */
public class YangProtoTypeMapper {

    private YangProtoTypeMapper() {}

    // -----------------------------------------------------------------------
    // Field-type resolution
    // -----------------------------------------------------------------------

    /**
     * Returns the protobuf field type for a YANG type using SIMPLE mode.
     * Convenience overload; prefer {@link #getProtoFieldType(Type, ProtoCodecMode)}.
     */
    public static DescriptorProtos.FieldDescriptorProto.Type getProtoType(Type yangType) {
        return getProtoFieldType(yangType, ProtoCodecMode.SIMPLE);
    }

    /**
     * Returns the protobuf field type for a YANG type in the given mode.
     *
     * <p>In YGOT mode, scalars are represented as {@code TYPE_MESSAGE};
     * callers should also call {@link #getYwrapperTypeName(Type)} to obtain
     * the fully-qualified wrapper message name for the {@code type_name} field.
     *
     * @param yangType the YANG type (may be {@code null})
     * @param mode     the codec mode
     * @return the protobuf field type; defaults to {@code TYPE_STRING} on
     *         unknown types
     */
    public static DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(
            Type yangType, ProtoCodecMode mode) {

        if (yangType == null) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }

        String typeName = getBaseTypeName(yangType.getRestriction());

        if (mode == ProtoCodecMode.YGOT) {
            return getProtoFieldTypeYgot(typeName);
        } else {
            return getProtoFieldTypeSimple(typeName);
        }
    }

    /**
     * In YGOT mode, scalar types map to {@code TYPE_MESSAGE} (wrapper messages).
     * Returns the fully-qualified ywrapper type name for the given type.
     *
     * @return e.g. {@code ".ywrapper.IntValue"}, or {@code null} for enum/bits
     *         types that generate their own descriptors
     */
    public static String getYwrapperTypeName(Type yangType) {
        if (yangType == null) return null;
        String typeName = getBaseTypeName(yangType.getRestriction());
        switch (typeName) {
            case "int8": case "int16": case "int32": case "int64":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.INT_VALUE;
            case "uint8": case "uint16": case "uint32": case "uint64":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.UINT_VALUE;
            case "boolean": case "empty":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.BOOL_VALUE;
            case "string": case "identityref":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.STRING_VALUE;
            case "decimal64":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.DECIMAL64_VALUE;
            case "binary":
                return "." + WrapperTypeManager.YWRAPPER_PACKAGE + "." + WrapperTypeManager.BYTES_VALUE;
            default:
                return null; // enum, bits, union have their own type names
        }
    }

    // YGOT field-type mapping: scalars → TYPE_MESSAGE (ywrapper)
    private static DescriptorProtos.FieldDescriptorProto.Type getProtoFieldTypeYgot(String typeName) {
        switch (typeName) {
            case "int8": case "int16": case "int32": case "int64":
            case "uint8": case "uint16": case "uint32": case "uint64":
            case "boolean": case "empty":
            case "string": case "decimal64": case "binary": case "identityref":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
            case "enumeration": case "bits":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
            case "leafref":
                // Resolved by caller; default to string wrapper
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
            case "union":
                // Handled as oneof in YGOT mode; use TYPE_MESSAGE as placeholder
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
            default:
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
    }

    // SIMPLE field-type mapping: scalars → primitive proto types
    private static DescriptorProtos.FieldDescriptorProto.Type getProtoFieldTypeSimple(String typeName) {
        switch (typeName) {
            case "int8": case "int16": case "int32":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
            case "int64":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
            case "uint8": case "uint16": case "uint32":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
            case "uint64":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
            case "boolean": case "empty":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
            case "string": case "decimal64": case "identityref":
            case "union": case "leafref":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "enumeration": case "bits":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
            case "binary":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
            default:
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
    }

    // -----------------------------------------------------------------------
    // Value conversion: YANG → Proto
    // -----------------------------------------------------------------------

    /**
     * Converts a YANG value to the appropriate protobuf value.
     * Returns native Java types that protobuf's reflection API accepts.
     *
     * @param value    the YANG value (string, Number, Boolean, etc.)
     * @param yangType the YANG type for the leaf
     * @return proto-compatible value
     */
    public static Object convertToProtoValue(Object value, Type yangType) {
        if (value == null) return null;

        if (yangType == null) {
            // Best-effort conversion without type info
            if (value instanceof Boolean) return value;
            if (value instanceof Number)  return value;
            return value.toString();
        }

        String typeName = getBaseTypeName(yangType.getRestriction());
        switch (typeName) {
            case "int8": case "int16": case "int32":
                return toInt(value);
            case "int64":
                return toLong(value);
            case "uint8": case "uint16": case "uint32":
                return toUnsignedInt(value);
            case "uint64":
                return toUnsignedLong(value);
            case "boolean":
                return toBoolean(value);
            case "decimal64":
                return toDecimalString(value);
            case "binary":
                return toByteString(value);
            case "enumeration":
                return toEnumInt(value, (Enumeration) yangType.getRestriction());
            case "empty":
                return true; // presence → true
            case "bits":
                return value.toString(); // SIMPLE: string representation
            case "identityref":
            case "leafref":
            case "union":
            case "string":
            default:
                return value.toString();
        }
    }

    // -----------------------------------------------------------------------
    // Value conversion: Proto → YANG
    // -----------------------------------------------------------------------

    /**
     * Converts a protobuf value back to a YANG-compatible representation.
     *
     * @param value    the protobuf value
     * @param yangType the target YANG type
     * @return YANG-compatible value (String for most types)
     */
    public static Object convertToYangValue(Object value, Type yangType) {
        if (value == null) return null;

        if (yangType == null) {
            if (value instanceof Boolean) return value;
            if (value instanceof Number)  return value;
            return value.toString();
        }

        String typeName = getBaseTypeName(yangType.getRestriction());
        switch (typeName) {
            case "int8":
                return String.valueOf(((Number) value).byteValue());
            case "int16":
                return String.valueOf(((Number) value).shortValue());
            case "int32":
                return String.valueOf(((Number) value).intValue());
            case "int64":
                return String.valueOf(((Number) value).longValue());
            case "uint8":
                return String.valueOf(((Number) value).intValue() & 0xFF);
            case "uint16":
                return String.valueOf(((Number) value).intValue() & 0xFFFF);
            case "uint32":
                return String.valueOf(Integer.toUnsignedLong(((Number) value).intValue()));
            case "uint64":
                // proto stores uint64 as long with two's complement bit pattern
                return Long.toUnsignedString(((Number) value).longValue());
            case "boolean":
                return value.toString();
            case "decimal64":
                return value.toString(); // already a string
            case "binary":
                if (value instanceof ByteString) {
                    return Base64.getEncoder().encodeToString(((ByteString) value).toByteArray());
                }
                return value.toString();
            case "enumeration":
                return fromEnumInt(value, (Enumeration) yangType.getRestriction());
            case "empty":
                return ""; // YANG empty value is empty string
            case "bits":
            case "identityref":
            case "leafref":
            case "union":
            case "string":
            default:
                return value.toString();
        }
    }

    // -----------------------------------------------------------------------
    // Type name resolution
    // -----------------------------------------------------------------------

    /**
     * Returns the canonical YANG built-in type name for a restriction,
     * using specific subtype checks (not the buggy generic YangInteger check).
     */
    public static String getBaseTypeName(Restriction<?> restriction) {
        if (restriction == null) return "string";
        // Integer subtypes must be checked before the generic YangInteger check
        if (restriction instanceof Int8)    return "int8";
        if (restriction instanceof Int16)   return "int16";
        if (restriction instanceof Int32)   return "int32";
        if (restriction instanceof Int64)   return "int64";
        if (restriction instanceof UInt8)   return "uint8";
        if (restriction instanceof UInt16)  return "uint16";
        if (restriction instanceof UInt32)  return "uint32";
        if (restriction instanceof UInt64)  return "uint64";
        if (restriction instanceof YangBoolean) return "boolean";
        if (restriction instanceof Decimal64)   return "decimal64";
        if (restriction instanceof Enumeration) return "enumeration";
        if (restriction instanceof Bits)        return "bits";
        if (restriction instanceof Binary)      return "binary";
        if (restriction instanceof YangString)  return "string";
        if (restriction instanceof Empty)       return "empty";
        if (restriction instanceof IdentityRef) return "identityref";
        if (restriction instanceof LeafRef)     return "leafref";
        if (restriction instanceof Union)       return "union";
        // Fallback for any YangInteger not caught above (shouldn't happen)
        if (restriction instanceof YangInteger) return "int64";
        return "string";
    }

    // -----------------------------------------------------------------------
    // Private conversion helpers
    // -----------------------------------------------------------------------

    private static int toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        return Integer.parseInt(v.toString().trim());
    }

    private static long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseLong(v.toString().trim());
    }

    private static int toUnsignedInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        return (int) Long.parseUnsignedLong(v.toString().trim());
    }

    private static long toUnsignedLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseUnsignedLong(v.toString().trim());
    }

    /**
     * Fixed boolean conversion: only "true" and "1" are true.
     * Previous version incorrectly returned true for "false" and "0".
     */
    private static boolean toBoolean(Object v) {
        if (v instanceof Boolean) return (Boolean) v;
        String s = v.toString().trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private static String toDecimalString(Object v) {
        if (v instanceof BigDecimal) return ((BigDecimal) v).toPlainString();
        return v.toString();
    }

    private static ByteString toByteString(Object v) {
        if (v instanceof ByteString) return (ByteString) v;
        if (v instanceof byte[])     return ByteString.copyFrom((byte[]) v);
        if (v instanceof String) {
            try {
                return ByteString.copyFrom(Base64.getDecoder().decode((String) v));
            } catch (IllegalArgumentException e) {
                return ByteString.copyFromUtf8((String) v);
            }
        }
        return ByteString.copyFromUtf8(v.toString());
    }

    /**
     * Converts a YANG enumeration value (name string) to its integer value.
     * Falls back to 0 only when the name is not found in the restriction.
     */
    private static int toEnumInt(Object value, Enumeration enumRestriction) {
        String name = value.toString();
        if (enumRestriction != null) {
            try {
                Integer intVal = enumRestriction.getEnumActualValue(name);
                if (intVal != null) return intVal;
            } catch (Exception ignored) {}
            // Try parsing as integer (value may already be an int)
        }
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Converts a protobuf enum integer back to a YANG enumeration name.
     */
    private static String fromEnumInt(Object value, Enumeration enumRestriction) {
        if (enumRestriction == null) return value.toString();
        int intVal = ((Number) value).intValue();
        try {
            java.util.List<org.yangcentral.yangkit.model.api.stmt.type.YangEnum> enums =
                    enumRestriction.getEffectiveEnums();
            if (enums != null) {
                for (org.yangcentral.yangkit.model.api.stmt.type.YangEnum e : enums) {
                    Integer ev = enumRestriction.getEnumActualValue(e.getArgStr());
                    if (ev != null && ev == intVal) {
                        return e.getArgStr();
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.valueOf(intVal);
    }

    // -----------------------------------------------------------------------
    // Decimal64 helper for YGOT mode
    // -----------------------------------------------------------------------

    /**
     * Converts a decimal64 string value into (digits, precision) for
     * {@code ywrapper.Decimal64Value} encoding.
     *
     * @param decimalString the decimal string, e.g. {@code "3.14"}
     * @param fractionDigits the YANG fraction-digits value
     * @return {@code long[2]} where {@code [0]=digits} and {@code [1]=precision}
     */
    public static long[] toDecimal64Parts(String decimalString, int fractionDigits) {
        try {
            BigDecimal bd = new BigDecimal(decimalString.trim())
                    .setScale(fractionDigits, java.math.RoundingMode.HALF_UP);
            BigInteger unscaled = bd.unscaledValue();
            return new long[]{unscaled.longValue(), fractionDigits};
        } catch (Exception e) {
            return new long[]{0L, fractionDigits};
        }
    }

    /**
     * Converts a (digits, precision) pair back to a decimal string.
     */
    public static String fromDecimal64Parts(long digits, int precision) {
        BigDecimal bd = new BigDecimal(BigInteger.valueOf(digits), precision);
        return bd.toPlainString();
    }
}
