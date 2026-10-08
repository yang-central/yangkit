package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationSupport;
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
            com.fasterxml.jackson.databind.JsonNode jsonNode =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree((String) payloadValue);
            if (!jsonNode.isObject()) {
                AnydataValidationSupport.recordInvalidContent(
                        getSchemaNode(), getSourcePath(), payloadValue,
                        "JSON content must be an object.", validatorResultBuilder);
                return data;
            }
            if (jsonNode.isEmpty()) {
                return data;
            }
            YangSchemaContext payloadSchemaContext = AnydataValidationSupport.resolveSchemaContext(
                    getSchemaNode(), getSourcePath(), getSchemaContext(),
                    getAnydataValidationContextResolver(), payloadValue, validatorResultBuilder);
            if (payloadSchemaContext == null) {
                return data;
            }
            YangDataDocumentJsonCodec documentJsonCodec = new YangDataDocumentJsonCodec(payloadSchemaContext);
            YangDataDocument dataDocument = documentJsonCodec.deserialize(jsonNode, validatorResultBuilder,
                    getAnydataValidationContextResolver());
            data.setValue(dataDocument);
        } catch (JsonProcessingException exception) {
            AnydataValidationSupport.recordInvalidContent(
                    getSchemaNode(), getSourcePath(), payloadValue,
                    "invalid JSON: " + exception.getOriginalMessage(), validatorResultBuilder);
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
                YangDataDocumentJsonCodec documentJsonCodec =
                        new YangDataDocumentJsonCodec(document.getSchemaContext());
                builder.setField(valueField, documentJsonCodec.serialize(document).toString());
            }
        }
        return builder;
    }
}
