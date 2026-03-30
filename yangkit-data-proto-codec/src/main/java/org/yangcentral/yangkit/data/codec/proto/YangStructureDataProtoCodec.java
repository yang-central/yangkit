package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.data.impl.model.YangStructureDataImpl;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

/** Codec for YANG {@code sx:structure} data (RFC 8791). */
public class YangStructureDataProtoCodec
        extends YangDataProtoCodec<YangStructure, YangStructureData> {

    protected YangStructureDataProtoCodec(YangStructure schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected YangStructureData buildData(DynamicMessage message,
                                          ValidatorResultBuilder validatorResultBuilder) {
        YangStructureDataImpl data = new YangStructureDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder);
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for structure: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
