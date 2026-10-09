package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorAppTag;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContext;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationRequest;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Unique;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    public void deserializeWithoutOptionsFallsBackToDocumentSchema() throws Exception {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildDocumentJson(), validator);
        assertNotNull(document);
        AnyDataData anyDataData = extractAnydata(document);
        assertNotNull(anyDataData.getValue());
        assertTrue(anyDataData.getValue().getDataChildren().isEmpty());
        assertTrue(validator.build().getRecords().stream().noneMatch(
                record -> record.getErrorMsg() != null
                        && record.getErrorMsg().getMessage().contains("No payload schema")));
    }

    @Test
    public void deserializeWithUnmatchedOptionsFallsBackToDocumentSchema() throws Exception {
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(buildDocumentJson(), validator, new AnydataValidationOptions());

        assertNotNull(extractAnydata(document).getValue());
        assertTrue(validator.build().getRecords().stream().noneMatch(
                record -> record.getErrorMsg() != null
                        && record.getErrorMsg().getMessage().contains("No payload schema")));
    }

    @Test
    public void deserializeWithStrictOptionsReportsMissingPayloadSchema() throws Exception {
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(buildDocumentJson(), validator,
                        new AnydataValidationOptions().requirePayloadSchema(true));

        assertNull(extractAnydata(document).getValue());
        assertEquals(ErrorTag.OPERATION_FAILED, validator.build().getRecords().get(0).getErrorTag());
    }

    @Test
    public void deserializeWithCustomStrictResolverReportsMissingPayloadSchema() throws Exception {
        AnydataValidationContextResolver resolver = new AnydataValidationContextResolver() {
            @Override
            public AnydataValidationContext resolve(AnydataValidationRequest request) {
                return null;
            }

            @Override
            public boolean isRequirePayloadSchema() {
                return true;
            }
        };
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(buildDocumentJson(), validator, resolver);

        assertNull(extractAnydata(document).getValue());
        assertEquals(ErrorTag.OPERATION_FAILED, validator.build().getRecords().get(0).getErrorTag());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() throws Exception {
        YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .requirePayloadSchema(true)
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
    public void validateAnydataInKeyedListRebasesFullErrorPath() throws Exception {
        String json = "{\"outer-anydata:anydata-wrapper\":{\"entry\":[{\"name\":\"x\","
                + "\"payload-holder\":{\"payload-anydata:payload-root\":{\"value\":\"wrong\"}}}]}}";
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(new ObjectMapper().readTree(json), validator,
                        new AnydataValidationOptions().registerSchemaContext(
                                PAYLOAD_HOLDER_QNAME, payloadSchemaContext));

        assertTrue(validator.build().isOk());
        ValidatorResult validationResult = document.validate();
        assertFalse(validationResult.isOk());
        assertTrue(validationResult.getRecords().stream().anyMatch(record ->
                "/outer:entry[outer:name = 'x']/outer:payload-holder/payload:value"
                        .equals(String.valueOf(record.getErrorPath()))), validationResult.toString());
        assertTrue(validationResult.getRecords().stream().anyMatch(record ->
                "/outer:entry[outer:name = 'x']/outer:payload-holder"
                        .equals(String.valueOf(record.getErrorPath()))), validationResult.toString());
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

    @Test
    public void deserializeNullAnydataReportsBadElement() throws Exception {
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(buildDocumentJson("null"), validator);

        assertNotNull(document);
        assertEquals(ErrorTag.BAD_ELEMENT, validator.build().getRecords().get(0).getErrorTag());
    }

    @Test
    public void duplicateUniqueValueInPayloadListReportsValidationError() throws Exception {
        ValidatorResult result = validatePayloadWithItemNames("shared", "shared");

        assertFalse(result.isOk());
        assertTrue(result.getRecords().stream().anyMatch(record ->
                ErrorAppTag.DATA_NOT_UNIQUE.getName().equals(record.getErrorAppTag())));
    }

    @Test
    public void distinctUniqueValuesInPayloadListValidate() throws Exception {
        assertTrue(validatePayloadWithItemNames("first", "second").isOk());
    }

    @Test
    public void payloadSchemaPathsAreBuiltBeforeDeserialization() {
        YangList list = payloadList();
        Leaf leaf = list.getUniques().get(0).getUniqueNodes().get(0);
        assertNotNull(list.getSchemaPath());
        assertNotNull(leaf.getSchemaPath());
        assertEquals("/payload:payload-root/payload:item", list.getSchemaPath().toString());
        assertEquals("/payload:payload-root/payload:item/payload:details/payload:name",
                leaf.getSchemaPath().toString());
    }

    @Test
    public void ordinaryListUniqueValidationUsesBuiltSchemaPaths() throws Exception {
        ValidatorResultBuilder parseResult = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(payloadSchemaContext).deserialize(
                new ObjectMapper().readTree("{\"payload-anydata:payload-root\":{\"item\":["
                        + "{\"id\":\"one\",\"details\":{\"name\":\"shared\"}},"
                        + "{\"id\":\"two\",\"details\":{\"name\":\"shared\"}}]}}"), parseResult);
        assertTrue(parseResult.build().isOk());
        ValidatorResult result = document.validate();
        assertFalse(result.isOk());
        assertTrue(result.getRecords().stream().anyMatch(record ->
                ErrorAppTag.DATA_NOT_UNIQUE.getName().equals(record.getErrorAppTag())));
    }

    @Test
    public void omittedUniqueLeafDoesNotCountAsDuplicate() throws Exception {
        ValidatorResultBuilder parseResult = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(payloadSchemaContext).deserialize(
                new ObjectMapper().readTree("{\"payload-anydata:payload-root\":{\"item\":["
                        + "{\"id\":\"one\"},{\"id\":\"two\"}]}}"), parseResult);
        assertTrue(parseResult.build().isOk());
        assertTrue(document.validate().isOk());
    }

    @Test
    public void uniqueLeafOutsideListReportsValidationError() throws Exception {
        SchemaNodeContainer root = (SchemaNodeContainer) payloadSchemaContext.getTreeNodeChild(
                new QName("urn:test:payload-anydata", "payload-root"));
        assertInvalidUniqueLeaf((Leaf) root.getTreeNodeChild(
                new QName("urn:test:payload-anydata", "value")));
    }

    @Test
    public void uniqueLeafWithoutSchemaPathReportsValidationError() throws Exception {
        Leaf original = payloadList().getUniques().get(0).getUniqueNodes().get(0);
        Leaf missingPath = (Leaf) Proxy.newProxyInstance(Leaf.class.getClassLoader(),
                new Class<?>[]{Leaf.class}, (proxy, method, args) -> {
                    if ("getSchemaPath".equals(method.getName())) {
                        return null;
                    }
                    try {
                        return method.invoke(original, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        assertInvalidUniqueLeaf(missingPath);
    }

    @Test
    public void listWithoutSchemaPathReportsValidationError() throws Exception {
        YangList list = payloadList();
        Field supportField = org.yangcentral.yangkit.model.impl.stmt.DataDefinitionImpl.class
                .getDeclaredField("schemaNodeSupport");
        supportField.setAccessible(true);
        Object support = supportField.get(list);
        Field pathField = support.getClass().getDeclaredField("schemaPath");
        pathField.setAccessible(true);
        Object originalPath = pathField.get(support);
        try {
            pathField.set(support, null);
            assertInvalidUniqueLeaf(list.getUniques().get(0).getUniqueNodes().get(0));
        } finally {
            pathField.set(support, originalPath);
        }
    }

    @Test
    public void invalidUniqueConstraintDoesNotPreventLaterConstraintValidation() throws Exception {
        YangList list = payloadList();
        Unique unique = list.getUniques().get(0);
        Leaf original = unique.getUniqueNodes().get(0);
        SchemaNodeContainer root = (SchemaNodeContainer) payloadSchemaContext.getTreeNodeChild(
                new QName("urn:test:payload-anydata", "payload-root"));
        Unique later = new org.yangcentral.yangkit.model.impl.stmt.UniqueImpl("details/name");
        later.addUniqueNode(original);
        try {
            unique.removeUniqueNodes();
            unique.addUniqueNode((Leaf) root.getTreeNodeChild(
                    new QName("urn:test:payload-anydata", "value")));
            list.getUniques().add(later);
            ValidatorResult result = assertDoesNotThrow(
                    () -> validatePayloadWithItemNames("shared", "shared"));
            assertFalse(result.isOk());
            assertTrue(result.getRecords().stream().anyMatch(record ->
                    ErrorTag.OPERATION_FAILED.equals(record.getErrorTag())
                            && record.getErrorMsg().getMessage().contains("Cannot resolve unique")));
            assertTrue(result.getRecords().stream().anyMatch(record ->
                    ErrorAppTag.DATA_NOT_UNIQUE.getName().equals(record.getErrorAppTag())));
        } finally {
            list.getUniques().remove(later);
            unique.removeUniqueNodes();
            unique.addUniqueNode(original);
        }
    }

    private void assertInvalidUniqueLeaf(Leaf leaf) throws Exception {
        Unique unique = payloadList().getUniques().get(0);
        Leaf original = unique.getUniqueNodes().get(0);
        try {
            unique.removeUniqueNodes();
            assertTrue(unique.addUniqueNode(leaf));
            ValidatorResult result = assertDoesNotThrow(
                    () -> validatePayloadWithItemNames("shared", "shared"));
            assertFalse(result.isOk());
            assertTrue(result.getRecords().stream().anyMatch(record ->
                    ErrorTag.OPERATION_FAILED.equals(record.getErrorTag())
                            && record.getErrorPath() != null
                            && record.getBadElement() != null
                            && record.getErrorMsg().getMessage().contains("details/name")));
            assertFalse(result.getRecords().stream().anyMatch(record ->
                    ErrorAppTag.DATA_NOT_UNIQUE.getName().equals(record.getErrorAppTag())));
        } finally {
            unique.removeUniqueNodes();
            unique.addUniqueNode(original);
        }
    }

    private YangList payloadList() {
        SchemaNodeContainer root = (SchemaNodeContainer) payloadSchemaContext.getTreeNodeChild(
                new QName("urn:test:payload-anydata", "payload-root"));
        return (YangList) root.getTreeNodeChild(new QName("urn:test:payload-anydata", "item"));
    }

    private ValidatorResult validatePayloadWithItemNames(String firstName, String secondName) throws Exception {
        String json = "{\"outer-anydata:anydata-wrapper\":{\"payload-holder\":{"
                + "\"payload-anydata:payload-root\":{\"item\":["
                + "{\"id\":\"one\",\"details\":{\"name\":\"" + firstName + "\"}},"
                + "{\"id\":\"two\",\"details\":{\"name\":\"" + secondName + "\"}}]}}}}";
        ValidatorResultBuilder parseResult = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext).deserialize(
                new ObjectMapper().readTree(json), parseResult,
                new AnydataValidationOptions().registerSchemaContext(
                        PAYLOAD_HOLDER_QNAME, payloadSchemaContext));
        assertTrue(parseResult.build().isOk());
        AnyDataData anydata = extractAnydata(document);
        assertNotNull(anydata.getValue());
        assertEquals(2, anydata.getValue().getDataChildren("item").size());
        assertSame(payloadList(), anydata.getValue().getDataChildren("item").get(0).getSchemaNode());
        return anydata.getValue().validate();
    }
}
