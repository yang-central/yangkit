package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

/**
 * Codec for YANG {@code anydata} nodes.
 *
 * <p>In YGOT mode anydata nodes are typically encoded as
 * {@code google.protobuf.Any} messages (wire-format compatible).
 * In SIMPLE mode they are encoded as an opaque {@code bytes} field.
 * The current implementation stores the content as an opaque object;
 * full structural round-tripping of arbitrary sub-trees is left to
 * higher-level layers.
 */
public class AnyDataDataProtoCodec extends YangDataProtoCodec<Anydata, AnyDataData> {

    protected AnyDataDataProtoCodec(Anydata schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected AnyDataData buildData(DynamicMessage message,
                                    ValidatorResultBuilder validatorResultBuilder) {
        AnyDataDataImpl data = new AnyDataDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for anydata: " + getSchemaNode().getIdentifier());
        return DynamicMessage.newBuilder(desc);
    }
}
