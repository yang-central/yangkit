package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

/** Codec for YANG {@code leaf} data. */
public class LeafDataProtoCodec extends YangDataProtoCodec<Leaf, LeafData<?>> {

    protected LeafDataProtoCodec(Leaf schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected LeafData<?> buildData(DynamicMessage message,
                                    ValidatorResultBuilder validatorResultBuilder) {
        if (message == null || message.getAllFields().isEmpty()) return null;

        // A leaf is encoded as a message with a single "value" field
        Descriptors.FieldDescriptor field =
                message.getDescriptorForType().findFieldByName("value");
        if (field == null && !message.getDescriptorForType().getFields().isEmpty()) {
            field = message.getDescriptorForType().getFields().get(0);
        }
        if (field == null) return null;

        Object rawValue = message.getField(field);
        Object yangValue = YangProtoTypeMapper.convertToYangValue(rawValue, getSchemaNode().getType());

        try {
            LeafData<?> leafData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(), yangValue != null ? yangValue.toString() : null);
            leafData.setQName(getSchemaNode().getIdentifier());
            return leafData;
        } catch (Exception e) {
            System.err.println("[LeafDataProtoCodec] Failed to build leaf data: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        LeafData<?> leafData = (LeafData<?>) yangData;
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) return null;

        DynamicMessage.Builder builder = DynamicMessage.newBuilder(desc);

        try {
            String strValue = leafData.getStringValue();
            if (strValue != null && !desc.getFields().isEmpty()) {
                Descriptors.FieldDescriptor field =
                        desc.findFieldByName("value") != null
                                ? desc.findFieldByName("value")
                                : desc.getFields().get(0);
                Object protoValue = YangProtoTypeMapper.convertToProtoValue(
                        strValue, getSchemaNode().getType());
                if (protoValue != null) {
                    builder.setField(field, protoValue);
                }
            }
        } catch (Exception e) {
            System.err.println("[LeafDataProtoCodec] Failed to build element: " + e.getMessage());
        }
        return builder;
    }
}
