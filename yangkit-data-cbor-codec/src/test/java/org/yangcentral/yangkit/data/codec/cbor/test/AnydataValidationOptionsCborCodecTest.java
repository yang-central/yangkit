package org.yangcentral.yangkit.data.codec.cbor.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
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
import org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec;
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

public class AnydataValidationOptionsCborCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;
    private static Container wrapperContainer;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsCborCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsCborCodecTest.class.getClassLoader()
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
    }

    private byte[] buildCbor(boolean valid) throws Exception {
        String payload = valid
                ? "\"value\":\"abc\",\"item\":[{\"id\":\"one\",\"name\":\"first\"}]"
                : "\"value\":\"wrong\",\"selected-target\":\"missing\"";
        String json = "{"
                + "\"payload-holder\":{"
                + "\"payload-anydata:payload-root\":{"
                + payload
                + "}"
                + "}"
                + "}";
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        return new ObjectMapper(new CBORFactory()).writeValueAsBytes(jsonNode);
    }

    private byte[] buildPrimitiveCbor(JsonNode primitiveValue) throws Exception {
        ObjectNode wrapper = new ObjectMapper().createObjectNode();
        wrapper.set("payload-holder", primitiveValue);
        return new ObjectMapper(new CBORFactory()).writeValueAsBytes(wrapper);
    }

    @Test
    public void deserializeWithoutOptionsReportsMissingPayloadSchema() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        ContainerData containerData = codec.deserialize(buildCbor(true), validator);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNull(anyDataData.getValue());
        assertFalse(validator.build().isOk());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        ContainerData containerData = codec.deserialize(buildCbor(true), validator, options);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(2, anyDataData.getValue().getDataChildren().size());
        assertEquals("value", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
        assertTrue(validator.build().isOk());
        assertTrue(containerData.validate().isOk());
    }

    @Test
    public void validateWithSchemaMappedOptionsReportsNestedConstraintFailures() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        ContainerData containerData = codec.deserialize(buildCbor(false), validator, options);

        assertNotNull(containerData);
        assertTrue(validator.build().isOk());
        ValidatorResult validationResult = containerData.validate();
        assertFalse(validationResult.isOk());
        assertTrue(validationResult.getRecords().size() >= 3);
    }

    @Test
    public void deserializePrimitiveValuesReportsBadElement() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode[] primitiveValues = {
                mapper.getNodeFactory().textNode("text"),
                mapper.getNodeFactory().numberNode(42),
                mapper.getNodeFactory().booleanNode(true),
                mapper.getNodeFactory().nullNode()
        };
        for (JsonNode primitiveValue : primitiveValues) {
            ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
            ValidatorResultBuilder validator = new ValidatorResultBuilder();

            ContainerData containerData = codec.deserialize(buildPrimitiveCbor(primitiveValue), validator);

            assertNotNull(containerData);
            ValidatorResult parseResult = validator.build();
            assertFalse(parseResult.isOk());
            ValidatorRecord<?, ?> record = parseResult.getRecords().get(0);
            assertEquals(ErrorTag.BAD_ELEMENT, record.getErrorTag());
            assertNotNull(record.getErrorPath());
            assertFalse(record.getErrorPath().toString().isEmpty());
            assertNotNull(record.getBadElement());
            assertTrue(record.getErrorMsg().getMessage().contains("decode to a map"));
        }
    }

    @Test
    public void deserializeEmptyMapAcceptsEmptyAnydata() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        ContainerData containerData = codec.deserialize(
                buildPrimitiveCbor(new ObjectMapper().createObjectNode()), validator);

        assertNotNull(containerData);
        assertTrue(validator.build().isOk());
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNull(anyDataData.getValue());
    }
}

