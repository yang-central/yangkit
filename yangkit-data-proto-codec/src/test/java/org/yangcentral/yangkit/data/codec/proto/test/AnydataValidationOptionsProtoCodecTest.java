package org.yangcentral.yangkit.data.codec.proto.test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.proto.ProtoDescriptorManager;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationOptionsProtoCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;
    private static Container wrapperContainer;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsProtoCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsProtoCodecTest.class.getClassLoader()
                .getResource("anydata-validation/payload/yang").getFile());
        assertTrue(outerSchemaContext.validate().isOk());
        assertTrue(payloadSchemaContext.validate().isOk());
        for (Module module : outerSchemaContext.getModules()) {
            if ("outer-anydata".equals(module.getArgStr())) {
                wrapperContainer = (Container) module.getDataNodeChildren().get(0);
                break;
            }
        }
        assertNotNull(wrapperContainer);
        ProtoDescriptorManager.getInstance().clearCache();
    }

    private DynamicMessage buildWrapperMessage(boolean valid) {
        Descriptors.Descriptor wrapperDescriptor = ProtoDescriptorManager.getInstance().getDescriptor(wrapperContainer);
        assertNotNull(wrapperDescriptor);
        Descriptors.FieldDescriptor payloadHolderField = wrapperDescriptor.findFieldByName("payload_holder");
        assertNotNull(payloadHolderField);

        DynamicMessage.Builder anydataBuilder = DynamicMessage.newBuilder(payloadHolderField.getMessageType());
        Descriptors.FieldDescriptor valueField = payloadHolderField.getMessageType().findFieldByName("value");
        assertNotNull(valueField);
        String payload = valid
                ? "{\"payload-anydata:payload-root\":{"
                        + "\"value\":\"abc\",\"item\":[{\"id\":\"one\",\"name\":\"first\"}]}}"
                : "{\"payload-anydata:payload-root\":{"
                        + "\"value\":\"wrong\",\"selected-target\":\"missing\"}}";
        anydataBuilder.setField(valueField, payload);

        DynamicMessage.Builder wrapperBuilder = DynamicMessage.newBuilder(wrapperDescriptor);
        wrapperBuilder.setField(payloadHolderField, anydataBuilder.build());
        return wrapperBuilder.build();
    }

    private DynamicMessage buildWrapperMessage(String payload) {
        Descriptors.Descriptor wrapperDescriptor = ProtoDescriptorManager.getInstance().getDescriptor(wrapperContainer);
        Descriptors.FieldDescriptor payloadHolderField = wrapperDescriptor.findFieldByName("payload_holder");
        DynamicMessage.Builder anydataBuilder = DynamicMessage.newBuilder(payloadHolderField.getMessageType());
        Descriptors.FieldDescriptor valueField = payloadHolderField.getMessageType().findFieldByName("value");
        anydataBuilder.setField(valueField, payload);
        return DynamicMessage.newBuilder(wrapperDescriptor)
                .setField(payloadHolderField, anydataBuilder.build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private ContainerData deserialize(
            DynamicMessage message,
            AnydataValidationOptions options,
            ValidatorResultBuilder validator) {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(wrapperContainer);
        if (options == null) {
            return (ContainerData) ((YangDataProtoCodec<Container, ContainerData>) codec).deserialize(message, validator);
        }
        return (ContainerData) ((YangDataProtoCodec<Container, ContainerData>) codec).deserialize(message, validator, options);
    }

    @Test
    public void deserializeWithoutOptionsReportsMissingPayloadSchema() {
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        ContainerData containerData = deserialize(buildWrapperMessage(true), null, validator);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNull(anyDataData.getValue());
        assertFalse(validator.build().isOk());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() {
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        ContainerData containerData = deserialize(buildWrapperMessage(true), options, validator);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(2, anyDataData.getValue().getDataChildren().size());
        assertEquals("value", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
        assertTrue(validator.build().isOk());
        assertTrue(containerData.validate().isOk());
    }

    @Test
    public void validateWithSchemaMappedOptionsReportsNestedConstraintFailures() {
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        ContainerData containerData = deserialize(buildWrapperMessage(false), options, validator);

        assertNotNull(containerData);
        assertTrue(validator.build().isOk());
        ValidatorResult validationResult = containerData.validate();
        assertFalse(validationResult.isOk());
        assertTrue(validationResult.getRecords().size() >= 3);
    }

    @Test
    public void deserializePrimitiveValuesReportsBadElement() {
        String[] primitiveValues = {"\"text\"", "42", "true", "null"};
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);
        for (String primitiveValue : primitiveValues) {
            ValidatorResultBuilder validator = new ValidatorResultBuilder();

            ContainerData containerData = deserialize(buildWrapperMessage(primitiveValue), options, validator);

            assertNotNull(containerData);
            ValidatorResult parseResult = validator.build();
            assertFalse(parseResult.isOk());
            ValidatorRecord<?, ?> record = parseResult.getRecords().get(0);
            assertEquals(ErrorTag.BAD_ELEMENT, record.getErrorTag());
            assertNotNull(record.getErrorPath());
            assertFalse(record.getErrorPath().toString().isEmpty());
            assertEquals(primitiveValue, record.getBadElement());
            assertTrue(record.getErrorMsg().getMessage().contains("must be an object"));
        }
    }

    @Test
    public void deserializeEmptyObjectAcceptsEmptyAnydata() {
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        ContainerData containerData = deserialize(buildWrapperMessage("{}"), null, validator);

        assertNotNull(containerData);
        assertTrue(validator.build().isOk());
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNull(anyDataData.getValue());
    }
}

