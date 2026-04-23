package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.data.codec.proto.WrapperTypeManager;
import org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper;
import org.yangcentral.yangkit.model.api.stmt.Type;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/** YGOT-compatible convention: wrapper messages, FNV-1a field numbers, bits→enum, union→oneof. */
public class YgotProtoConvention implements YangProtoConvention {

    @Override public String getName() { return "ygot"; }

    @Override
    public DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(Type yangType) {
        String w = getWrapperTypeName(yangType);
        return w != null ? DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
                         : YangProtoTypeMapper.getProtoType(yangType);
    }

    @Override
    public String getWrapperTypeName(Type yangType) {
        return YangProtoTypeMapper.getYwrapperTypeName(yangType);
    }

    @Override public boolean encodeBitsAsEnum()   { return true; }
    @Override public boolean encodeUnionAsOneof() { return true; }

    @Override
    public int nextFieldNumber(String fieldPath, Set<Integer> usedNumbers) {
        return fnvFieldNumber(fieldPath, usedNumbers);
    }

    /** Package-visible for use by {@link YangProtoConventionLoader}. */
    static int fnvFieldNumber(String path, Set<Integer> usedNumbers) {
        long hash = 2166136261L;
        for (byte b : path.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xFF);
            hash = (hash * 16777619L) & 0xFFFFFFFFL;
        }
        int num = (int) (hash % 536870911L) + 1;
        if (num >= 19000 && num <= 19999) num = 20000;
        while (usedNumbers.contains(num)) num++;
        usedNumbers.add(num);
        return num;
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

    @Override
    public Descriptors.FileDescriptor[] getDependencies() {
        try {
            Descriptors.FileDescriptor fd = WrapperTypeManager.getInstance().getFileDescriptor();
            return fd != null ? new Descriptors.FileDescriptor[]{fd}
                              : new Descriptors.FileDescriptor[0];
        } catch (Exception e) {
            return new Descriptors.FileDescriptor[0];
        }
    }

    @Override
    public String[] getDependencyImports() {
        return new String[]{WrapperTypeManager.YWRAPPER_FILE};
    }
}





