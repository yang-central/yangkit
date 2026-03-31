package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.impl.model.ListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.YangList;

/** Codec for YANG {@code list} data. */
public class ListDataProtoCodec extends YangDataProtoCodec<YangList, ListData> {

    protected ListDataProtoCodec(YangList schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected ListData buildData(DynamicMessage message,
                                 ValidatorResultBuilder validatorResultBuilder) {
        ListDataImpl data = new ListDataImpl(getSchemaNode(), null);
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder, mode,
                getAnydataValidationContextResolver(), getSourcePath());
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) {
            throw new RuntimeException(
                    "No descriptor for list: " + getSchemaNode().getIdentifier());
        }
        return DynamicMessage.newBuilder(desc);
    }
}
