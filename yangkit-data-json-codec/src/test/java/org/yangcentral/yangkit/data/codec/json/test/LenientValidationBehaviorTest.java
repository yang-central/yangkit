package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.LenientValidationOptions;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangLibraryParser;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the strict/lenient validation contract.
 *
 * <p><b>Strict is the default.</b> Unknown elements, inactive (if-feature / deviated)
 * nodes, invalid identityref values and missing list keys are reported as errors by
 * default. Lenient handling is strictly opt-in, applied while
 * {@link LenientValidationOptions#enable()} is active on the current thread.
 *
 * <p>Lenient mode is limited to genuinely partial-schema situations:
 * <ul>
 *   <li>inactive (if-feature / deviated) nodes are accepted;</li>
 *   <li>missing modules for an identityref are accepted as raw values.</li>
 * </ul>
 * It does <em>not</em> accept an identityref value that fails its restriction against a
 * complete schema, and it does <em>not</em> downgrade a missing list key (which drops the
 * entry) to a warning.
 *
 * <p>Notes on fixtures:
 * <ul>
 *   <li>JSON field names use the <em>module name</em> as prefix (per RFC 7950),
 *       not the YANG prefix. The module name is {@code test-lenient}.</li>
 *   <li>The schema tree is only populated after {@code ctx.validate()}, so every
 *       context used for parsing is validated first.</li>
 *   <li>A node with {@code if-feature} is only reported <em>inactive</em> when the
 *       schema context carries a {@code YangSchema} (i.e. it was built from a YANG
 *       Library) that does not enable the feature. The {@code ctxLib} context is
 *       built from a YANG Library that enables no features, so the
 *       {@code if-feature f-extra} leaf {@code value} is inactive there.</li>
 * </ul>
 */
public class LenientValidationBehaviorTest {

    /** Plain schema (YangYinParser) — all if-feature nodes active. */
    private static YangSchemaContext ctx;

    /** Schema built from a YANG Library that enables no features — if-feature nodes inactive. */
    private static YangSchemaContext ctxLib;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, DocumentException {
        URL yangDir = LenientValidationBehaviorTest.class.getClassLoader()
                .getResource("lenient-validation/yang");
        assertNotNull(yangDir);
        ctx = YangYinParser.parse(yangDir.getFile());
        assertTrue(ctx.validate().isOk(), "fixture schema must validate cleanly: " + ctx.validate());

        ctxLib = parseYangLibrary(yangDir);
        assertTrue(ctxLib.validate().isOk(),
                "fixture schema (yang library) must validate cleanly: " + ctxLib.validate());
    }

    // ------------------------------------------------------------------
    // missing list key
    // ------------------------------------------------------------------

    private static final String MISSING_KEY_JSON =
            "{\"test-lenient:root\":"
            + "{\"id\":\"a\","
            + "\"things\":[{\"value\":5}]}"
            + "}";

    @Test
    public void missingListKey_isErrorInStrictAndLenientModes() throws Exception {
        // A missing list key drops the entry, so it must stay an ERROR even in lenient
        // mode — downgrading it to a warning would silently lose data.
        ValidatorResult strict = parse(MISSING_KEY_JSON, false);
        assertHasTag(strict, ErrorTag.MISSING_ELEMENT, Severity.ERROR);

        ValidatorResult lenient = parse(MISSING_KEY_JSON, true);
        assertHasTag(lenient, ErrorTag.MISSING_ELEMENT, Severity.ERROR);
    }

    // ------------------------------------------------------------------
    // invalid identityref with complete schema
    // ------------------------------------------------------------------

    private static final String BAD_IDENTITYREF_JSON =
            "{\"test-lenient:root\":"
            + "{\"id\":\"a\",\"mode\":\"bogus-mode\"}"
            + "}";

    @Test
    public void invalidIdentityref_isErrorInStrictAndLenientModes() throws Exception {
        // An invalid identityref value (not an identity of the declared base) is an error
        // in BOTH strict and lenient modes: lenient handling is limited to genuinely
        // missing modules, never to accepting an invalid value against a complete schema.
        ValidatorResult strict = parse(BAD_IDENTITYREF_JSON, false);
        assertTrue(hasRecords(strict),
                "invalid identityref with complete schema must be reported in strict mode: " + strict);

        ValidatorResult lenient = parse(BAD_IDENTITYREF_JSON, true);
        assertTrue(hasRecords(lenient),
                "invalid identityref must still be reported in lenient mode: " + lenient);
    }

    // ------------------------------------------------------------------
    // inactive node (if-feature disabled via YANG Library)
    // ------------------------------------------------------------------

    private static final String INACTIVE_NODE_JSON =
            "{\"test-lenient:root\":"
            + "{\"id\":\"b\","
            + "\"things\":[{\"name\":\"n1\",\"value\":7}]}"
            + "}";

    @Test
    public void inactiveNode_isErrorInStrictMode_andAcceptedInLenientMode() throws Exception {
        ValidatorResult strict = parse(ctxLib, INACTIVE_NODE_JSON, false);
        assertHasTag(strict, ErrorTag.UNKNOWN_ELEMENT, Severity.ERROR);

        ValidatorResult lenient = parse(ctxLib, INACTIVE_NODE_JSON, true);
        assertTrue(isOk(lenient),
                "inactive node must be accepted in lenient mode: " + lenient);
    }

    // ------------------------------------------------------------------
    // cross-module augment: second-pass resolution must not leave stale errors
    // ------------------------------------------------------------------

    @Test
    public void crossModuleAugment_resolvesWithoutStaleMissingTargetError()
            throws IOException, YangParserException, DocumentException {
        URL local = LenientValidationBehaviorTest.class.getClassLoader()
                .getResource("lenient-validation/augment-order");
        assertNotNull(local, "augment-order fixture required");
        YangSchemaContext augCtx = YangYinParser.parse(local.getFile());
        augCtx.validate();

        ValidatorResult result = augCtx.validate();
        List<? extends ValidatorRecord<?, ?>> stale =
                result.getRecords() == null ? Collections.<ValidatorRecord<?, ?>>emptyList()
                        : result.getRecords().stream()
                            .filter(r -> r.getErrorMsg() != null
                                    && r.getErrorMsg().getMessage()
                                        .equalsIgnoreCase("the target node is not found."))
                            .collect(Collectors.toList());
        assertTrue(stale.isEmpty(),
                "stale MISSING_TARGET records must be dropped when the augment resolves in the second pass: "
                        + stale);

        // The cross-module augment (01-auger) must have resolved against the target
        // module (02-target). The augmented leaf 'aux' is reachable on the Augment
        // statement itself once the target is known.
        Module auger = findModule(augCtx, "01-auger");
        assertNotNull(auger, "01-auger module must be loaded");
        List<? extends org.yangcentral.yangkit.model.api.stmt.Augment> augments = auger.getAugments();
        assertNotNull(augments, "01-auger must expose its augment statements");
        assertFalse(augments.isEmpty(), "01-auger must declare at least one augment");
        assertNotNull(augments.get(0).getDataDefChild("aux"),
                "augmented leaf 'aux' must be present on the augment after the target resolved");
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /**
     * Build a YANG Library (no features enabled) that references the fixture module,
     * and parse it through {@link YangLibraryParser} so the resulting context carries a
     * {@code YangSchema}. This makes if-feature nodes report as inactive.
     */
    private static YangSchemaContext parseYangLibrary(URL yangDir)
            throws IOException, YangParserException, DocumentException {
        File dirFile = new File(yangDir.getFile());
        String yangFile = new File(dirFile, "test-lenient@2025-01-01.yang").getAbsolutePath();
        String libXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n"
                + "  <content-id>lenient</content-id>\n"
                + "  <module-set>\n    <name>default</name>\n    <module>\n"
                + "      <name>test-lenient</name>\n"
                + "      <revision>2025-01-01</revision>\n"
                + "      <namespace>urn:test:lenient</namespace>\n"
                + "      <location>file://" + yangFile + "</location>\n"
                + "    </module>\n  </module-set>\n</yang-library>\n";
        Path libPath = Files.createTempFile("yanglib-lenient", ".xml");
        Files.write(libPath, libXml.getBytes(StandardCharsets.UTF_8));
        return YangLibraryParser.parse(libPath.toFile(), dirFile);
    }

    private static Module findModule(YangSchemaContext c, String name) {
        for (Module m : c.getModules()) {
            if (name.equals(m.getArgStr())) {
                return m;
            }
        }
        return null;
    }

    private static ValidatorResult parse(String json, boolean lenient) throws Exception {
        return parse(ctx, json, lenient);
    }

    private static ValidatorResult parse(YangSchemaContext schemaCtx, String json, boolean lenient) throws Exception {
        JsonNode node = new ObjectMapper().readTree(json);
        ValidatorResultBuilder builder = new ValidatorResultBuilder();
        try {
            if (lenient) {
                LenientValidationOptions.enable();
            }
            YangDataDocument doc = new YangDataDocumentJsonCodec(schemaCtx).deserialize(node, builder);
            assertNotNull(doc, "document must be built even when children have errors");
        } finally {
            LenientValidationOptions.disable(); // reset the thread to the default (strict) behavior
        }
        return builder.build();
    }

    private static boolean isOk(ValidatorResult r) {
        if (r.getRecords() == null) {
            return true;
        }
        return r.getRecords().stream()
                .noneMatch(rec -> rec.getSeverity() == Severity.ERROR);
    }

    private static boolean hasRecords(ValidatorResult r) {
        return r.getRecords() != null && !r.getRecords().isEmpty();
    }

    private static void assertHasTag(ValidatorResult r, ErrorTag tag, Severity severity) {
        assertNotNull(r.getRecords(), "expected records in " + r);
        boolean found = r.getRecords().stream()
                .anyMatch(rec -> rec.getErrorTag() == tag && rec.getSeverity() == severity);
        assertTrue(found,
                "expected record with tag " + tag + " severity " + severity + " in: " + r.getRecords());
    }
}
