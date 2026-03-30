package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

/** Codec for YANG {@code leaf-list} data. */
public class LeafListDataProtoCodec extends YangDataProtoCodec<LeafList, LeafListData<?>> {

    protected LeafListDataProtoCodec(LeafList schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected LeafListData<?> buildData(DynamicMessage message,
                                        ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) return null;

        Descriptors.FieldDescriptor field =
                message.getDescriptorForType().findFieldByName("value");
        if (field == null && !message.getDescriptorForType().getFields().isEmpty()) {
            field = message.getDescriptorForType().getFields().get(0);
        }
        if (field == null) return null;

        Object rawValue  = message.getField(field);
        Object yangValue = YangProtoTypeMapper.convertToYangValue(
                rawValue, getSchemaNode().getType());

        try {
            LeafListData<?> data = (LeafListData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(),
                            yangValue != null ? yangValue.toString() : null);
            data.setQName(getSchemaNode().getIdentifier());
            return data;
        } catch (Exception e) {
            System.err.println("[LeafListDataProtoCodec] Failed to build: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        LeafListData<?> data = (LeafListData<?>) yangData;
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) return null;

        DynamicMessage.Builder builder = DynamicMessage.newBuilder(desc);
        try {
            String strValue = data.getStringValue();
            if (strValue != null && !desc.getFields().isEmpty()) {
                Descriptors.FieldDescriptor field =
                        desc.findFieldByName("value") != null
                                ? desc.findFieldByName("value")
                                : desc.getFields().get(0);
                Object protoValue = YangProtoTypeMapper.convertToProtoValue(
                        strValue, getSchemaNode().getType());
                if (protoValue != null) builder.setField(field, protoValue);
            }
        } catch (Exception e) {
            System.err.println("[LeafListDataProtoCodec] Failed to build element: " + e.getMessage());
        }
        return builder;
    }
}
