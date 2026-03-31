package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;

/** Codec for YANG {@code container} data. */
public class ContainerDataProtoCodec extends YangDataProtoCodec<Container, ContainerData> {

    protected ContainerDataProtoCodec(Container schemaNode, ProtoCodecMode mode) {
        super(schemaNode, mode);
    }

    @Override
    protected ContainerData buildData(DynamicMessage message,
                                      ValidatorResultBuilder validatorResultBuilder) {
        ContainerDataImpl data = new ContainerDataImpl(getSchemaNode());
        data.setQName(getSchemaNode().getIdentifier());
        ProtoCodecUtil.deserializeChildren(data, message, validatorResultBuilder, mode,
                getAnydataValidationContextResolver(), getSourcePath());
        return data;
    }

    @Override
    protected Message.Builder buildElement(org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) {
            throw new RuntimeException(
                    "No descriptor for container: " + getSchemaNode().getIdentifier());
        }
        return DynamicMessage.newBuilder(desc);
    }
}
