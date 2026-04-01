package org.yangcentral.yangkit.data.codec.proto;

/**
 * Defines the codec mode for YANG-to-Protobuf encoding.
 *
 * <p>Two modes are supported:
 * <ul>
 *   <li>{@code SIMPLE} - Uses primitive protobuf types (int32, string, bool, etc.)
 *       with sequential field numbers. This mode is simpler and more straightforward.</li>
 *   <li>{@code YGOT} - Uses ywrapper.*Value wrapper messages for scalars and
 *       FNV-1a hash-based field numbers. This mode follows the subset of ygot-style
 *       wrapper and field-numbering conventions implemented by this module; it does
 *       not claim full wire compatibility with every ygot-generated schema.</li>
 * </ul>
 */
public enum ProtoCodecMode {
    /**
     * Simple mode using primitive protobuf types.
     * Scalars map directly to proto3 primitives:
     * int8/16/32 → int32, int64 → int64, uint8/16/32 → uint32, uint64 → uint64,
     * boolean → bool, string → string, binary → bytes, etc.
     */
    SIMPLE,

    /**
     * YGOT mode using wrapper messages for scalars.
     * Supported scalar families are wrapped in ywrapper.*Value messages:
     * IntValue, UintValue, BoolValue, StringValue, Decimal64Value, BytesValue.
     * Enum and bits values remain enum fields, and unrecognized derived types
     * fall back to the regular mapper behavior.
     * Field numbers are computed using FNV-1a hash of the schema path.
     */
    YGOT
}
