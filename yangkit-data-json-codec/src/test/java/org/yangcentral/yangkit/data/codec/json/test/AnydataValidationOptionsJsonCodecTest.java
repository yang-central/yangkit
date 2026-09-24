package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationOptionsJsonCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsJsonCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsJsonCodecTest.class.getClassLoader()
                .getResource("anydata-validation/payload/yang").getFile());
        assertTrue(outerSchemaContext.validate().isOk());
        assertTrue(payloadSchemaContext.validate().isOk());
    }

    private JsonNode buildDocumentJson() throws Exception {
        String json = "{"
                + "\"outer-anydata:anydata-wrapper\":{"
                + "\"payload-holder\":{"
                + "\"payload-anydata:payload-root\":{"
                + "\"value\":\"abc\","
                + "\"item\":[{\"id\":\"one\",\"name\":\"first\"}]"
                + "}"
                + "}"
                + "}"
                + "}";
        return new ObjectMapper().readTree(json);
    }

    private JsonNode buildDocumentJson(String anydataValue) throws Exception {
        String json = "{"
                + "\"outer-anydata:anydata-wrapper\":{"
                + "\"payload-holder\":" + anydataValue
                + "}"
                + "}";
        return new ObjectMapper().readTree(json);
    }

    private AnyDataData extractAnydata(YangDataContainer container) {
        YangData<?> firstChild = container.getDataChildren().get(0);
        if (firstChild instanceof AnyDataData) {
            return (AnyDataData) firstChild;
        }
        return (AnyDataData) ((YangDataContainer) firstChild).getDataChildren().get(0);
    }

    @Test
    public void deserializeWithoutOptionsReportsMissingPayloadSchema() throws Exception {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildDocumentJson(), validator);
        assertNotNull(document);
        AnyDataData anyDataData = extractAnydata(document);
        assertNull(anyDataData.getValue());
        ValidatorResult parseResult = validator.build();
        assertFalse(parseResult.isOk());
        assertTrue(parseResult.getRecords().get(0).getErrorMsg().getMessage().contains("No payload schema"));
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() throws Exception {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        YangDataDocument document = codec.deserialize(buildDocumentJson(), validator, options);
        assertNotNull(document);
        AnyDataData anyDataData = extractAnydata(document);
        assertNotNull(anyDataData.getValue());
        assertEquals(2, anyDataData.getValue().getDataChildren().size());
        assertEquals("value", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
        assertTrue(validator.build().isOk());
        assertTrue(document.validate().isOk());
    }

    @Test
    public void validateWithSchemaMappedOptionsReportsNestedConstraintFailures() throws Exception {
        String invalidJson = "{"
                + "\"outer-anydata:anydata-wrapper\":{"
                + "\"payload-holder\":{"
                + "\"payload-anydata:payload-root\":{"
                + "\"value\":\"wrong\","
                + "\"selected-target\":\"missing\""
                + "}"
                + "}"
                + "}"
                + "}";
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        YangDataDocument document =
                codec.deserialize(new ObjectMapper().readTree(invalidJson), validator, options);

        assertNotNull(document);
        assertTrue(validator.build().isOk());
        ValidatorResult validationResult = document.validate();
        assertFalse(validationResult.isOk());
        assertTrue(validationResult.getRecords().size() >= 3);
        for (ValidatorRecord<?, ?> record : validationResult.getRecords()) {
            if (record.getErrorPath() != null) {
                assertTrue(record.getErrorPath().toString().contains("payload-holder"));
            }
        }
    }

    @Test
    public void deserializePrimitiveValuesReportsBadElement() throws Exception {
        String[] primitiveValues = {"\"text\"", "42", "true", "null"};
        for (String primitiveValue : primitiveValues) {
            YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
            ValidatorResultBuilder validator = new ValidatorResultBuilder();

            YangDataDocument document = codec.deserialize(buildDocumentJson(primitiveValue), validator);

            assertNotNull(document);
            ValidatorResult parseResult = validator.build();
            assertFalse(parseResult.isOk());
            ValidatorRecord<?, ?> record = parseResult.getRecords().get(0);
            assertEquals(ErrorTag.BAD_ELEMENT, record.getErrorTag());
            assertNotNull(record.getErrorPath());
            assertFalse(record.getErrorPath().toString().isEmpty());
            assertNotNull(record.getBadElement());
            assertTrue(record.getErrorMsg().getMessage().contains("must be an object"));
        }
    }

    @Test
    public void deserializeEmptyObjectAcceptsEmptyAnydata() throws Exception {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildDocumentJson("{}"), validator);

        assertNotNull(document);
        assertTrue(validator.build().isOk());
        assertNull(extractAnydata(document).getValue());
    }
}
