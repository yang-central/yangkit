package org.yangcentral.yangkit.data.codec.xml.test;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationOptionsXmlCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final String PAYLOAD_NS = "urn:test:payload-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsXmlCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsXmlCodecTest.class.getClassLoader()
                .getResource("anydata-validation/payload/yang").getFile());
        assertTrue(outerSchemaContext.validate().isOk());
        assertTrue(payloadSchemaContext.validate().isOk());
    }

    private Document buildDocument(boolean valid) throws Exception {
        String payload = valid
                ? "<value>abc</value>"
                        + "<item><id>one</id><name>first</name></item>"
                : "<value>wrong</value><selected-target>missing</selected-target>";
        String xml = "<anydata-wrapper xmlns=\"" + OUTER_NS + "\">"
                + "<payload-holder>"
                + "<payload-root xmlns=\"" + PAYLOAD_NS + "\">"
                + payload
                + "</payload-root>"
                + "</payload-holder>"
                + "</anydata-wrapper>";
        return DocumentHelper.parseText(xml);
    }

    private Document buildTextDocument(String value) throws Exception {
        String xml = "<anydata-wrapper xmlns=\"" + OUTER_NS + "\">"
                + "<payload-holder>" + value + "</payload-holder>"
                + "</anydata-wrapper>";
        return DocumentHelper.parseText(xml);
    }

    private AnyDataData extractAnydata(YangDataDocument document) {
        return (AnyDataData) document.getDataChildren().get(0);
    }

    @Test
    public void deserializeWithoutOptionsReportsMissingPayloadSchema() throws Exception {
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildDocument(true), validator);

        assertNotNull(document);
        assertNull(extractAnydata(document).getValue());
        assertFalse(validator.build().isOk());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAndValidatesAnydataPayload() throws Exception {
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        YangDataDocument document = codec.deserialize(buildDocument(true), validator, options);

        assertNotNull(document);
        AnyDataData anyDataData = extractAnydata(document);
        assertNotNull(anyDataData.getValue());
        assertEquals(1, anyDataData.getValue().getDataChildren().size());
        YangDataContainer payloadRoot =
                (YangDataContainer) anyDataData.getValue().getDataChildren().get(0);
        YangDataContainer item = (YangDataContainer) payloadRoot.getDataChildren("item").get(0);
        assertEquals(2, item.getDataChildren().size());
        ValidatorResult parseResult = validator.build();
        assertTrue(parseResult.isOk(), parseResult.print(Severity.ERROR));
        ValidatorResult validationResult = document.validate();
        assertTrue(validationResult.isOk(), validationResult.print(Severity.ERROR));
    }

    @Test
    public void validateWithSchemaMappedOptionsReportsNestedConstraintFailures() throws Exception {
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        YangDataDocument document = codec.deserialize(buildDocument(false), validator, options);

        assertNotNull(document);
        ValidatorResult parseResult = validator.build();
        assertTrue(parseResult.isOk(), parseResult.print(Severity.ERROR));
        ValidatorResult validationResult = document.validate();
        assertFalse(validationResult.isOk());
        assertTrue(validationResult.getRecords().size() >= 2);
        for (ValidatorRecord<?, ?> record : validationResult.getRecords()) {
            if (record.getErrorPath() != null) {
                assertTrue(record.getErrorPath().toString().contains("payload-holder"));
            }
        }
    }

    @Test
    public void deserializeTextOnlyValuesReportsBadElement() throws Exception {
        String[] primitiveValues = {"text", "  text  "};
        for (String primitiveValue : primitiveValues) {
            YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
            ValidatorResultBuilder validator = new ValidatorResultBuilder();

            YangDataDocument document = codec.deserialize(buildTextDocument(primitiveValue), validator);

            assertNotNull(document);
            ValidatorResult parseResult = validator.build();
            assertFalse(parseResult.isOk());
            ValidatorRecord<?, ?> record = parseResult.getRecords().get(0);
            assertEquals(ErrorTag.BAD_ELEMENT, record.getErrorTag());
            assertNotNull(record.getErrorPath());
            assertFalse(record.getErrorPath().toString().isEmpty());
            assertNotNull(record.getBadElement());
            assertTrue(record.getErrorMsg().getMessage().contains("child elements"));
        }
    }

    @Test
    public void deserializeEmptyElementAcceptsEmptyAnydata() throws Exception {
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildTextDocument(""), validator);

        assertNotNull(document);
        assertTrue(validator.build().isOk());
        assertNull(extractAnydata(document).getValue());
    }
}
