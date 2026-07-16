/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yangcentral.yangkit.examples;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Example: build a {@link YangSchemaContext} from an RFC 8525 YANG Library XML file
 * and a directory of YANG module files.
 *
 * <p>Demonstrates the three {@code parseFromYangLibrary} overloads added by the
 * Yang Library feature:
 * <ol>
 *   <li>{@link YangYinParser#parseFromYangLibrary(File, File)} — File objects</li>
 *   <li>{@link YangYinParser#parseFromYangLibrary(InputStream, File)} — InputStream</li>
 *   <li>{@link YangYinParser#parseFromYangLibrary(String, String)} — path strings</li>
 * </ol>
 *
 * <p>This example is self-contained within yangkit — no Kafka, no Schema Registry,
 * no external services. The same {@link YangSchemaContext} produced here can be used
 * with any yangkit data-codec (JSON, XML, CBOR) to parse or validate YANG instance data.
 *
 * <p>Directory layout expected for this example:
 * <pre>
 *   resources/yang-library-example/
 *     yang-library.xml          &lt;!-- RFC 8525 yang-library document --&gt;
 *     modules/
 *       ietf-interfaces@2018-02-20.yang
 *       ietf-ip@2018-02-22.yang
 *       ... (all modules referenced in yang-library.xml)
 * </pre>
 */
public class App5YangLibrary {

    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------------
        // Locate example resources
        // ------------------------------------------------------------------
        File exampleDir   = new File("yangkit-examples/src/main/resources/yang-library-example");
        File xmlFile      = new File(exampleDir, "yang-library.xml");
        File modulesDir   = new File(exampleDir, "modules");

        // ------------------------------------------------------------------
        // Approach 1: File overload
        // ------------------------------------------------------------------
        System.out.println("=== Approach 1: parseFromYangLibrary(File, File) ===");
        YangSchemaContext ctx1 = YangYinParser.parseFromYangLibrary(xmlFile, modulesDir);
        printContextSummary(ctx1);

        // ------------------------------------------------------------------
        // Approach 2: InputStream overload
        // ------------------------------------------------------------------
        System.out.println("\n=== Approach 2: parseFromYangLibrary(InputStream, File) ===");
        try (InputStream xmlStream = new FileInputStream(xmlFile)) {
            YangSchemaContext ctx2 = YangYinParser.parseFromYangLibrary(xmlStream, modulesDir);
            printContextSummary(ctx2);
        }

        // ------------------------------------------------------------------
        // Approach 3: String path overload
        // ------------------------------------------------------------------
        System.out.println("\n=== Approach 3: parseFromYangLibrary(String, String) ===");
        YangSchemaContext ctx3 = YangYinParser.parseFromYangLibrary(
                xmlFile.getAbsolutePath(),
                modulesDir.getAbsolutePath());
        printContextSummary(ctx3);
    }

    private static void printContextSummary(YangSchemaContext ctx) {
        List<Module> modules = ctx.getModules();
        System.out.println("Loaded " + modules.size() + " module(s):");
        for (Module m : modules) {
            System.out.println("  " + m.getArgStr()
                    + (m.getMainModule() != null && m.getMainModule().getNamespace() != null
                       ? "  ns=" + m.getMainModule().getNamespace().getUri()
                       : ""));
        }
    }
}
