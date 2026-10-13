package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import java.io.File;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangLibraryParser;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: the YANG Library parser must not silently substitute a different revision
 * of a module. When the library requests a specific revision, only files declaring that
 * exact revision are accepted.
 */
public class YangLibraryRevisionMatchTest {

    private File fixtureDir() {
        URL dir = YangLibraryRevisionMatchTest.class.getClassLoader()
                .getResource("yang-library-revision");
        assertNotNull(dir);
        return new File(dir.getFile());
    }

    private File copyLibraryWithLocation(String xmlName, Path tmpDir) throws IOException {
        Path modulesDir = tmpDir.resolve("modules");
        Files.createDirectories(modulesDir);
        String xml = new String(Files.readAllBytes(fixtureDir().toPath().resolve(xmlName)),
                StandardCharsets.UTF_8);
        // point the explicit location at this JVM's actual module copy
        File moduleFile = new File(fixtureDir(), "modules/test-rev@2024-01-01.yang");
        String location = "file://" + moduleFile.getAbsolutePath();
        xml = xml.replace("file://REPLACE_WITH_MODULES_DIR/test-rev@2024-01-01.yang", location);
        Path out = tmpDir.resolve(xmlName);
        Files.write(out, xml.getBytes(StandardCharsets.UTF_8));
        return out.toFile();
    }

    @Test
    public void matchingRevisionIsAccepted()
            throws IOException, DocumentException, YangParserException {
        Path tmp = Files.createTempDirectory("yanglib-match");
        File xml = copyLibraryWithLocation("yang-lib-match.xml", tmp);

        try {
            YangSchemaContext ctx = YangLibraryParser.parse(xml, new File(fixtureDir(), "modules"));
            assertEquals(1, ctx.getModules().size());
        } finally {
            deleteRecursively(tmp);
        }
    }

    @Test
    public void mismatchingRevisionIsRejected() throws IOException {
        Path tmp = Files.createTempDirectory("yanglib-mismatch");
        File xml = copyLibraryWithLocation("yang-lib-mismatch.xml", tmp);

        try {
            // yang-lib requests 2023-01-01 but the only available file declares 2024-01-01:
            // the name@revision candidate does not exist and falling back to unversioned
            // filenames is forbidden, so loading must fail.
            IOException e = assertThrows(IOException.class,
                    () -> YangLibraryParser.parse(xml, new File(fixtureDir(), "modules")));
            assertTrue(e.getMessage().contains("Cannot locate") || e.getMessage().contains("Revision mismatch"),
                    "unexpected failure: " + e.getMessage());
        } finally {
            deleteRecursively(tmp);
        }
    }

    @Test
    public void fileWithWrongContentRevisionIsRejected() throws IOException {
        // library requests 2023 (not 2024) but an explicitly located file declares 2024
        Path tmp = Files.createTempDirectory("yanglib-wrongcontent");
        Path fakeModule = tmp.resolve("test-rev@2023-01-01.yang");
        String yang = new String(Files.readAllBytes(
                fixtureDir().toPath().resolve("modules/test-rev@2024-01-01.yang")),
                StandardCharsets.UTF_8);
        Files.write(fakeModule, yang.getBytes(StandardCharsets.UTF_8));
        File xml = new File(tmp.toFile(), "yang-lib.xml");
        String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n"
                + "  <content-id>t</content-id>\n"
                + "  <module-set>\n    <name>default</name>\n    <module>\n"
                + "      <name>test-rev</name>\n"
                + "      <revision>2023-01-01</revision>\n"
                + "      <namespace>urn:test:rev</namespace>\n"
                + "      <location>file://" + fakeModule.toAbsolutePath() + "</location>\n"
                + "    </module>\n  </module-set>\n</yang-library>\n";
        Files.write(xml.toPath(), xmlText.getBytes(StandardCharsets.UTF_8));

        try {
            IOException e = assertThrows(IOException.class,
                    () -> YangLibraryParser.parse(xml, new File(tmp.toFile(), "does-not-exist")));
            assertTrue(e.getMessage().contains("Revision mismatch"),
                    "expected revision mismatch but got: " + e.getMessage());
        } finally {
            deleteRecursively(tmp);
        }
    }

    private static void deleteRecursively(Path path) {
        try {
            if (path == null || !Files.exists(path)) {
                return;
            }
            Files.walk(path).sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
        }
    }
}
