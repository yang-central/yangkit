package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.util.Set;

/** Simple convention: primitive proto types, sequential field numbers, no wrapper messages. */
public class SimpleProtoConvention implements YangProtoConvention {

    @Override public String getName() { return "simple"; }

    @Override
    public DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(Type yangType) {
        return YangProtoTypeMapper.getProtoFieldType(yangType, org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode.SIMPLE);
    }

    @Override public boolean encodeBitsAsEnum()   { return false; }
    @Override public boolean encodeUnionAsOneof() { return false; }

    @Override
    public int nextFieldNumber(String fieldPath, Set<Integer> usedNumbers) {
        int n = 1;
        while (usedNumbers.contains(n)) n++;
        usedNumbers.add(n);
        return n;
    }

    @Override
    public Object toProtoValue(Object yangValue, Type yangType) {
        return YangProtoTypeMapper.convertToProtoValue(
                yangValue != null ? yangValue.toString() : null, yangType);
    }

    @Override
    public Object toYangValue(Object protoValue, Type yangType) {
        return YangProtoTypeMapper.convertToYangValue(protoValue, yangType);
    }
}

