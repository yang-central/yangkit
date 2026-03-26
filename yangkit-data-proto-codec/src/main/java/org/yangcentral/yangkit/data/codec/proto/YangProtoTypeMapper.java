package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Type mapper between YANG types and Protobuf types.
 * Handles conversion of values between the two type systems.
 */
public class YangProtoTypeMapper {
    
    private static final Map<String, DescriptorProtos.FieldDescriptorProto.Type> TYPE_MAPPING;
    
    static {
        TYPE_MAPPING = new HashMap<>();
        
        // Integer types
        TYPE_MAPPING.put("int8", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        TYPE_MAPPING.put("int16", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        TYPE_MAPPING.put("int32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        TYPE_MAPPING.put("int64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        
        // Unsigned integer types
        TYPE_MAPPING.put("uint8", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        TYPE_MAPPING.put("uint16", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        TYPE_MAPPING.put("uint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        TYPE_MAPPING.put("uint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64);
        
        // Other primitive types
        TYPE_MAPPING.put("boolean", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        TYPE_MAPPING.put("string", DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
        TYPE_MAPPING.put("decimal64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);
        TYPE_MAPPING.put("enumeration", DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM);
        TYPE_MAPPING.put("binary", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);
        TYPE_MAPPING.put("empty", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        
        // Time types
        TYPE_MAPPING.put("date-and-time", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        TYPE_MAPPING.put("date-only", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        TYPE_MAPPING.put("time-of-day", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
    }
    
    /**
     * Get Protobuf type from YANG type.
     * 
     * @param yangType the YANG type
     * @return corresponding Protobuf type
     */
    public static DescriptorProtos.FieldDescriptorProto.Type getProtoType(Type yangType) {
        if (yangType == null) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
        
        Restriction<?> restriction = yangType.getRestriction();
        if (restriction == null) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
        
        String typeName = getBaseTypeName(restriction);
        return TYPE_MAPPING.getOrDefault(typeName, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
    }
    
    /**
     * Convert YANG value to Protobuf value.
     * 
     * @param value the YANG value
     * @param yangType the YANG type
     * @return Protobuf-compatible value
     */
    public static Object convertToProtoValue(Object value, Type yangType) {
        if (value == null) {
            return null;
        }
        
        if (yangType == null) {
            // Try to infer the type from the value itself
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof Number) {
                return value;
            } else {
                return value.toString();
            }
        }
        
        Restriction<?> restriction = yangType.getRestriction();
        String typeName = getBaseTypeName(restriction);
        
        switch (typeName) {
            case "int8":
            case "int16":
            case "int32":
                return convertToInt(value);
            case "int64":
                return convertToLong(value);
            case "uint8":
            case "uint16":
            case "uint32":
                return convertToUnsignedInt(value);
            case "uint64":
                return convertToUnsignedLong(value);
            case "boolean":
                return convertToBoolean(value);
            case "decimal64":
                return convertToDouble(value);
            case "binary":
                return convertToBytes(value);
            case "enumeration":
                return convertEnumToInt(value, (Enumeration) restriction);
            case "empty":
                return value != null;
            case "date-and-time":
                return convertDateTimeToMillis(value);
            case "date-only":
                return convertDateToDays(value);
            case "time-of-day":
                return convertTimeToMillis(value);
            default:
                return value.toString();
        }
    }
    
    /**
     * Convert Protobuf value to YANG value.
     * 
     * @param value the Protobuf value
     * @param yangType the target YANG type
     * @return YANG-compatible value
     */
    public static Object convertToYangValue(Object value, Type yangType) {
        if (value == null) {
            return null;
        }
        
        if (yangType == null) {
            // Try to infer the type from the value itself
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof Number) {
                return value;
            } else {
                return value.toString();
            }
        }
        
        Restriction<?> restriction = yangType.getRestriction();
        String typeName = getBaseTypeName(restriction);
        
        switch (typeName) {
            case "int8":
                return ((Number) value).byteValue();
            case "int16":
                return ((Number) value).shortValue();
            case "int32":
                return ((Number) value).intValue();
            case "int64":
                return ((Number) value).longValue();
            case "uint8":
                return Integer.toUnsignedLong(((Number) value).intValue()) & 0xFF;
            case "uint16":
                return Integer.toUnsignedLong(((Number) value).intValue()) & 0xFFFF;
            case "uint32":
                return Integer.toUnsignedLong(((Number) value).intValue());
            case "uint64":
                return value; // Already unsigned long
            case "boolean":
                return value;
            case "decimal64":
                return new BigDecimal(value.toString());
            case "binary":
                return convertFromBytes(value);
            case "enumeration":
                return convertIntToEnum((Integer) value, (Enumeration) restriction);
            case "empty":
                return (Boolean) value ? "" : null;
            case "date-and-time":
                return convertMillisToDateTime((Long) value);
            case "date-only":
                return convertDaysToDate((Integer) value);
            case "time-of-day":
                return convertMillisToTime((Long) value);
            default:
                return value.toString();
        }
    }
    
    /**
     * Get base type name from restriction.
     */
    private static String getBaseTypeName(Restriction<?> restriction) {
        if (restriction instanceof YangInteger) {
            return "int32"; // Simplified - default to int32
        } else if (restriction instanceof YangBoolean) {
            return "boolean";
        } else if (restriction instanceof Decimal64) {
            return "decimal64";
        } else if (restriction instanceof Enumeration) {
            return "enumeration";
        } else if (restriction instanceof Binary) {
            return "binary";
        } else if (restriction instanceof YangString) {
            return "string";
        } else if (restriction instanceof Empty) {
            return "empty";
        }
        
        return restriction.getClass().getSimpleName();
    }
    
    // Conversion helper methods
    
    private static int convertToInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
    
    private static long convertToLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
    
    private static int convertToUnsignedInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseUnsignedInt(value.toString());
    }
    
    private static long convertToUnsignedLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseUnsignedLong(value.toString());
    }
    
    private static boolean convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(value.toString()) || 
               "1".equals(value.toString()) ||
               "false".equalsIgnoreCase(value.toString()) ||
               "0".equals(value.toString());
    }
    
    private static double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    private static ByteString convertToBytes(Object value) {
        if (value instanceof byte[]) {
            return ByteString.copyFrom((byte[]) value);
        } else if (value instanceof String) {
            byte[] decoded = Base64.getDecoder().decode((String) value);
            return ByteString.copyFrom(decoded);
        }
        return ByteString.copyFromUtf8(value.toString());
    }
    
    private static byte[] convertFromBytes(Object value) {
        if (value instanceof ByteString) {
            return ((ByteString) value).toByteArray();
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private static int convertEnumToInt(Object value, Enumeration enumeration) {
        // Simplified - try to parse as integer
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0; // Default value
        }
    }
    
    private static String convertIntToEnum(Integer value, Enumeration enumeration) {
        // Simplified - just return string representation
        return value.toString();
    }
    
    private static long convertDateTimeToMillis(Object value) {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toEpochSecond(ZoneOffset.UTC);
        } else if (value instanceof String) {
            return LocalDateTime.parse((String) value).toEpochSecond(ZoneOffset.UTC);
        }
        return System.currentTimeMillis();
    }
    
    private static LocalDateTime convertMillisToDateTime(Long millis) {
        return LocalDateTime.ofEpochSecond(millis, 0, ZoneOffset.UTC);
    }
    
    private static int convertDateToDays(Object value) {
        if (value instanceof LocalDate) {
            return (int) ((LocalDate) value).toEpochDay();
        } else if (value instanceof String) {
            return (int) LocalDate.parse((String) value).toEpochDay();
        }
        return 0;
    }
    
    private static LocalDate convertDaysToDate(Integer days) {
        return LocalDate.ofEpochDay(days);
    }
    
    private static long convertTimeToMillis(Object value) {
        if (value instanceof java.time.LocalTime) {
            java.time.LocalTime time = (java.time.LocalTime) value;
            return time.toSecondOfDay() * 1000;
        }
        return 0;
    }
    
    private static java.time.LocalTime convertMillisToTime(Long millis) {
        return java.time.LocalTime.ofSecondOfDay(millis / 1000);
    }
}
