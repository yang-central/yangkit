package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

/** Codec for YANG {@code anyxml} nodes. */
public class AnyxmlDataProtoCodec extends YangDataProtoCodec<Anyxml, AnyxmlData> {

    protected AnyxmlDataProtoCodec(Anyxml schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected AnyxmlData buildData(DynamicMessage message,
                                   ValidatorResultBuilder validatorResultBuilder) {
        AnyXmlDataImpl data = new AnyXmlDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for anyxml: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
