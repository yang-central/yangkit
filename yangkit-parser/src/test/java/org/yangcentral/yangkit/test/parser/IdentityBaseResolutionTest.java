package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdentityBaseResolutionTest {
    @TempDir
    Path tempDir;

    @Test
    public void unresolvedBaseIsReportedAndDerivedChecksAreNullSafe()
            throws IOException, YangParserException, DocumentException {
        File moduleFile = writeModule("unresolved-base.yang",
                "module unresolved-base {\n"
                        + "  yang-version 1.1;\n"
                        + "  namespace \"urn:test:unresolved-base\";\n"
                        + "  prefix ub;\n"
                        + "  identity child { base missing; }\n"
                        + "  identity other;\n"
                        + "}\n");

        YangSchemaContext context = YangYinParser.parse(Arrays.asList(moduleFile));
        ValidatorResult result = context.validate();
        assertFalse(result.isOk(), "An unresolved base must remain a schema error");

        Module module = context.getModule("unresolved-base", null).orElseThrow(AssertionError::new);
        Identity child = module.getIdentity("child");
        Identity other = module.getIdentity("other");
        assertNotNull(child);
        assertNotNull(other);
        assertFalse(child.isDerived(other));
        assertFalse(child.isDerivedOrSelf(other));
    }

    @Test
    public void baseInLaterImportedModuleResolvesDuringSchemaBuild()
            throws IOException, YangParserException, DocumentException {
        File derivedFile = writeModule("derived-identities.yang",
                "module derived-identities {\n"
                        + "  yang-version 1.1;\n"
                        + "  namespace \"urn:test:derived-identities\";\n"
                        + "  prefix di;\n"
                        + "  import base-identities { prefix bi; }\n"
                        + "  identity child { base bi:root; }\n"
                        + "}\n");
        File baseFile = writeModule("base-identities.yang",
                "module base-identities {\n"
                        + "  yang-version 1.1;\n"
                        + "  namespace \"urn:test:base-identities\";\n"
                        + "  prefix bi;\n"
                        + "  identity root;\n"
                        + "}\n");

        YangSchemaContext context = YangYinParser.parse(Arrays.asList(derivedFile, baseFile));
        ValidatorResult result = context.validate();
        assertTrue(result.isOk(), "A base imported from a later-loaded module should resolve: " + result);

        Identity child = context.getModule("derived-identities", null).orElseThrow(AssertionError::new)
                .getIdentity("child");
        Identity root = context.getModule("base-identities", null).orElseThrow(AssertionError::new)
                .getIdentity("root");
        assertTrue(child.isDerivedOrSelf(root));
    }

    private File writeModule(String name, String text) throws IOException {
        Path path = tempDir.resolve(name);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }
}