package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContext;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationRequest;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

/**
 * Codec for YANG {@code anydata} nodes.
 *
 * <p>The current implementation generates a wrapper message for the {@code anydata}
 * schema node and stores the payload JSON text in its {@code value} field.
 * Deserialization reuses the JSON document codec and resolves the payload schema
 * through the anydata validation context APIs.
 * This class does not currently expose protobuf-native {@code google.protobuf.Any}
 * encoding or claim general wire-format compatibility with external anydata layouts.
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
        Descriptors.FieldDescriptor valueField = message.getDescriptorForType().findFieldByName("value");
        if (valueField == null) {
            return data;
        }
        Object payloadValue = message.getField(valueField);
        if (!(payloadValue instanceof String) || ((String) payloadValue).isEmpty()) {
            return data;
        }

        try {
            YangSchemaContext payloadSchemaContext = getSchemaContext();
            if (getAnydataValidationContextResolver() != null) {
                AnydataValidationContext context = getAnydataValidationContextResolver().resolve(
                        new AnydataValidationRequest(getSchemaNode(), getSourcePath(), getSchemaContext()));
                if (context != null && context.getSchemaContext() != null) {
                    payloadSchemaContext = context.getSchemaContext();
                }
            }
            com.fasterxml.jackson.databind.JsonNode jsonNode =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree((String) payloadValue);
            YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(payloadSchemaContext);
            YangDataDocument dataDocument = documentJsonCodec.deserialize(jsonNode, validatorResultBuilder,
                    getAnydataValidationContextResolver());
            data.setValue(dataDocument);
        } catch (Exception ignored) {
        }
        return data;
    }

    @Override
    protected Message.Builder buildElement(
            org.yangcentral.yangkit.data.api.model.YangData<?> yangData) {
        Descriptors.Descriptor desc = getDescriptorForNode();
        if (desc == null) throw new RuntimeException(
                "No descriptor for anydata: " + getSchemaNode().getIdentifier());
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(desc);
        AnyDataData anyDataData = (AnyDataData) yangData;
        YangDataDocument document = anyDataData.getEffectiveValue();
        if (document != null) {
            Descriptors.FieldDescriptor valueField = desc.findFieldByName("value");
            if (valueField != null) {
                YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(getSchemaContext());
                builder.setField(valueField, documentJsonCodec.serialize(document).toString());
            }
        }
        return builder;
    }
}
