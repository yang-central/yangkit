package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sx:structure resolution in YangDataDocumentJsonParser.
 *
 * <p>The YANG {@code sx:structure} extension (RFC 8791) defines data structures
 * outside the normal YANG config/state data tree. Without the fix in
 * {@code YangDataUtil.getSchemaNodeContainerForDocument()}, the parser would
 * report {@code unknown-element} errors for any data nodes defined inside an
 * {@code sx:structure} block, because the default schema lookup only searches
 * the normal YANG data tree.</p>
 *
 * <p>This test uses a custom {@code test-structure} YANG module that defines
 * {@code sx:structure message} with containers and typed leaves, validating
 * that the parser correctly resolves these definitions and enforces type
 * constraints and mandatory leaf rules.</p>
 *
 * @see org.yangcentral.yangkit.data.impl.util.YangDataUtil#getSchemaNodeContainerForDocument
 */
public class JsonCodecStructureTest {

    private static YangSchemaContext schemaContext;

    @BeforeAll
    static void setUp() throws DocumentException, IOException, YangParserException {
        String yangDir = JsonCodecStructureTest.class.getClassLoader()
                .getResource("structure/yang").getFile();
        schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();

        assertTrue(result.isOk(), "YANG modules should parse without errors");
    }

    /**
     * Verifies that a complete, valid JSON document rooted at an sx:structure
     * node is parsed and validated successfully.
     *
     * <p>Before the fix, this would fail with {@code errorTag='unknown-element'}
     * because the parser could not find 'message' in the normal data tree.</p>
     */
    @Test
    public void validStructureMessage_shouldParseAndValidateSuccessfully()
            throws IOException {
        JsonNode json = loadJson("structure/json/valid_structure_message.json");

        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        YangDataDocumentJsonParser parser = new YangDataDocumentJsonParser(schemaContext);
        YangDataDocument document = parser.parse(json, resultBuilder);

        ValidatorResult parseResult = resultBuilder.build();
        assertNoErrors(parseResult, "Parsing valid structure message");

        assertNotNull(document, "Document should not be null for valid input");

        ValidatorResult validationResult = document.validate();
        assertNoErrors(validationResult, "Validating valid structure message");
    }

    /**
     * Verifies that a minimal valid JSON document (only mandatory fields)
     * is parsed and validated successfully within an sx:structure.
     */
    @Test
    public void validStructureMinimal_shouldParseAndValidateSuccessfully()
            throws IOException {
        JsonNode json = loadJson("structure/json/valid_structure_minimal.json");

        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        YangDataDocumentJsonParser parser = new YangDataDocumentJsonParser(schemaContext);
        YangDataDocument document = parser.parse(json, resultBuilder);

        ValidatorResult parseResult = resultBuilder.build();
        assertNoErrors(parseResult, "Parsing minimal structure message");

        assertNotNull(document, "Document should not be null for valid input");

        ValidatorResult validationResult = document.validate();
        assertNoErrors(validationResult, "Validating minimal structure message");
    }

    /**
     * Verifies that type constraint violations inside an sx:structure
     * are properly detected. The test provides a string value where
     * a uint32 is expected.
     *
     * <p>This proves the parser is not just accepting anything — it actually
     * resolves the sx:structure schema and enforces YANG type constraints.
     * The parser may throw a codec exception at runtime or report errors
     * in the validator result — either outcome confirms type enforcement.</p>
     */
    @Test
    public void invalidStructureType_shouldFailValidation() throws IOException {
        JsonNode json = loadJson("structure/json/invalid_structure_type.json");

        boolean errorDetected = false;
        try {
            ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
            YangDataDocumentJsonParser parser = new YangDataDocumentJsonParser(schemaContext);
            YangDataDocument document = parser.parse(json, resultBuilder);

            ValidatorResult parseResult = resultBuilder.build();
            errorDetected = hasErrors(parseResult);

            if (!errorDetected && document != null) {
                ValidatorResult validationResult = document.validate();
                errorDetected = hasErrors(validationResult);
            }
        } catch (Exception e) {
            // A codec/runtime exception during parsing is also a valid
            // way for the parser to reject invalid type values
            errorDetected = true;
        }

        assertTrue(errorDetected,
                "Invalid type values inside sx:structure should produce errors");
    }

    /**
     * Verifies that missing mandatory leaves inside an sx:structure
     * are properly detected during validation.
     *
     * <p>The 'timestamp' leaf in the metadata container is mandatory.
     * A valid document must include it. This test omits it and expects
     * a validation error.</p>
     */
    @Test
    public void invalidStructureMissingMandatory_shouldFailValidation()
            throws IOException {
        JsonNode json = loadJson("structure/json/invalid_structure_missing_mandatory.json");

        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        YangDataDocumentJsonParser parser = new YangDataDocumentJsonParser(schemaContext);
        YangDataDocument document = parser.parse(json, resultBuilder);

        // Missing mandatory leaves are typically caught in validate(), not parse()
        assertNotNull(document, "Document should be parseable even with missing mandatory leaves");

        ValidatorResult validationResult = document.validate();
        assertTrue(hasErrors(validationResult),
                "Missing mandatory leaf 'timestamp' should produce a validation error");
    }

    /**
     * Verifies that the existing notification push-update test data
     * (which also uses sx:structure via ietf-notification) can be
     * parsed through the document-level parser, resolving the
     * ietf-notification:notification sx:structure definition.
     *
     * <p>This is a regression test using the real notification YANG
     * modules already present in the test resources. Note: the full
     * notification message includes augmentations from other modules
     * (e.g. ietf-yang-push:push-update augmenting the notification
     * structure) which may produce warnings — this test focuses on
     * verifying the sx:structure root is resolved to a non-null
     * document, proving the BFS lookup works.</p>
     */
    @Test
    public void notificationPushUpdate_shouldParseViaDocumentParser()
            throws DocumentException, IOException, YangParserException {
        // Use the existing notification/pushupdate YANG modules
        String yangDir = this.getClass().getClassLoader()
                .getResource("notification/pushupdate/yang").getFile();
        YangSchemaContext notifContext = YangYinParser.parse(yangDir);
        notifContext.validate();

        // Parse the valid push-update notification through YangDataDocumentJsonParser.
        // Before the fix, this would return null or fail entirely because
        // "notification" is defined as sx:structure in ietf-notification,
        // not as a regular container in the YANG data tree.
        JsonNode json = loadJson("notification/pushupdate/json/valid_pu_notification.json");

        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        YangDataDocumentJsonParser parser = new YangDataDocumentJsonParser(notifContext);
        YangDataDocument document = parser.parse(json, resultBuilder);

        // The document should be non-null — proving that the parser found
        // the sx:structure "notification" definition via the BFS lookup.
        // Note: augmentations into the structure (e.g. push-update from
        // ietf-yang-push) may produce parse-level warnings; that is a
        // separate concern from sx:structure resolution.
        assertNotNull(document,
                "ietf-notification:notification (sx:structure) should be resolved correctly");
    }

    // --- Helpers ---

    private JsonNode loadJson(String resourcePath) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertNotNull(is, "Test resource not found: " + resourcePath);
        return new ObjectMapper().readTree(is);
    }

    private static void assertNoErrors(ValidatorResult result, String context) {
        if (result == null || result.getRecords() == null) return;

        List<? extends ValidatorRecord<?, ?>> errors = result.getRecords().stream()
                .filter(r -> r.getSeverity() == Severity.ERROR)
                .toList();

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(context).append(" - unexpected errors:\n");
            for (ValidatorRecord<?, ?> record : errors) {
                sb.append("  - ").append(record).append("\n");
            }
            fail(sb.toString());
        }
    }

    private static boolean hasErrors(ValidatorResult result) {
        if (result == null || result.getRecords() == null) return false;
        return result.getRecords().stream()
                .anyMatch(r -> r.getSeverity() == Severity.ERROR);
    }
}
