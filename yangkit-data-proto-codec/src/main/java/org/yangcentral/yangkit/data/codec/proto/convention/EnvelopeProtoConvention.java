package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.util.Set;

/**
 * Envelope convention: the entire YANG data payload is serialized as JSON or XML
 * and stored in a single proto field. The proto schema is simply:
 * <pre>{@code
 * message YangData {
 *   bytes data = 1;  // (or string data = 1)
 * }
 * }</pre>
 * After proto decoding, the inner payload is passed to the appropriate
 * JSON or XML codec for further deserialization into {@code YangData}.
 */
public class EnvelopeProtoConvention implements YangProtoConvention {

    private final String name;
    private final String innerFormat;    // "json" or "xml"
    private final DescriptorProtos.FieldDescriptorProto.Type fieldType;
    private final String fieldName;

    public EnvelopeProtoConvention(String name, String innerFormat,
                                   DescriptorProtos.FieldDescriptorProto.Type fieldType,
                                   String fieldName) {
        this.name        = name;
        this.innerFormat = innerFormat;
        this.fieldType   = fieldType;
        this.fieldName   = fieldName;
    }

    @Override public String getName()  { return name; }
    @Override public boolean isEnvelope() { return true; }
    @Override public String getEnvelopeInnerFormat() { return innerFormat; }
    @Override public DescriptorProtos.FieldDescriptorProto.Type getEnvelopeFieldType() { return fieldType; }
    @Override public String getEnvelopeFieldName() { return fieldName; }

    @Override
    public DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(Type yangType) {
        return fieldType; // single payload field
    }

    @Override
    public int nextFieldNumber(String fieldPath, Set<Integer> usedNumbers) {
        return 1; // envelope always uses field number 1
    }

    @Override
    public Object toProtoValue(Object yangValue, Type yangType) {
        return yangValue != null ? yangValue.toString() : null;
    }

    @Override
    public Object toYangValue(Object protoValue, Type yangType) {
        return protoValue;
    }
}

