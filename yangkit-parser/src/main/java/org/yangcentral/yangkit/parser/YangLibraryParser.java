package org.yangcentral.yangkit.parser;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.ModuleSet;
import org.yangcentral.yangkit.model.api.schema.YangModuleDescription;
import org.yangcentral.yangkit.model.api.schema.YangSchema;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.register.YangStatementImplRegister;
import org.yangcentral.yangkit.register.YangStatementRegister;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser that loads a {@link YangSchemaContext} from a YANG Library XML document
 * (RFC 8525 &lt;yang-library&gt;) and a directory of YANG/YIN module files.
 *
 * <p>Usage:
 * <pre>
 *   YangSchemaContext ctx = YangLibraryParser.parse(
 *       new File("/path/to/yang-library.xml"),
 *       new File("/path/to/yang/modules"));
 *   ctx.validate();
 * </pre>
 *
 * <p>Module files are located in the following order for each module entry:
 * <ol>
 *   <li>If a {@code <location>} element with a {@code file://} URI is present in the
 *       yang-library XML, that path is used directly.</li>
 *   <li>Otherwise the yangSearchPath directory is searched for
 *       {@code module-name@revision.yang}, then {@code module-name.yang}.</li>
 * </ol>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8525">RFC 8525 – YANG Library</a>
 */
public class YangLibraryParser {

    private static final Logger logger = LoggerFactory.getLogger(YangLibraryParser.class);

    // -------------------------------------------------------------------------
    // Public entry-points
    // -------------------------------------------------------------------------

    /**
     * Parse a YANG Library from a file and load module files from a search directory.
     *
     * @param xmlFile        path to the RFC 8525 yang-library XML file
     * @param yangSearchPath directory that contains the .yang / .yin module files
     * @return populated, unvalidated {@link YangSchemaContext}
     */
    public static YangSchemaContext parse(File xmlFile, File yangSearchPath)
            throws DocumentException, IOException, YangParserException {
        SAXReader reader = SAXReader.createDefault();
        Document doc = reader.read(xmlFile);
        return parseRoot(doc.getRootElement(), yangSearchPath);
    }

    /**
     * Parse a YANG Library from an {@link InputStream} and load module files from a
     * search directory.
     *
     * @param xmlStream      RFC 8525 yang-library XML as a stream
     * @param yangSearchPath directory that contains the .yang / .yin module files
     * @return populated, unvalidated {@link YangSchemaContext}
     */
    public static YangSchemaContext parse(InputStream xmlStream, File yangSearchPath)
            throws DocumentException, IOException, YangParserException {
        SAXReader reader = SAXReader.createDefault();
        Document doc = reader.read(xmlStream);
        return parseRoot(doc.getRootElement(), yangSearchPath);
    }

    /**
     * Parse a YANG Library from a file path string and load module files from a
     * search directory path string.
     *
     * @param xmlFilePath    path string to the RFC 8525 yang-library XML file
     * @param yangSearchPath path string to the directory containing .yang / .yin files
     * @return populated, unvalidated {@link YangSchemaContext}
     */
    public static YangSchemaContext parse(String xmlFilePath, String yangSearchPath)
            throws DocumentException, IOException, YangParserException {
        return parse(new File(xmlFilePath),
                yangSearchPath != null ? new File(yangSearchPath) : null);
    }

    // -------------------------------------------------------------------------
    // Core implementation
    // -------------------------------------------------------------------------

    private static YangSchemaContext parseRoot(Element root, File yangSearchPath)
            throws IOException, YangParserException, DocumentException {

        List<ParsedModuleSet> moduleSets = parseModuleSets(root);

        YangStatementImplRegister.registerImpl();
        YangSchemaContext context =
                YangStatementRegister.getInstance().getSchemeContextInstance();

        // 1. Load import-only modules first so they are available as dependencies
        //    when the main modules are parsed and their imports are resolved.
        for (ParsedModuleSet ms : moduleSets) {
            for (ParsedModule m : ms.importOnlyModules) {
                loadModule(m, true, yangSearchPath, context);
            }
        }

        // 2. Load main (non-import-only) modules.
        for (ParsedModuleSet ms : moduleSets) {
            for (ParsedModule m : ms.modules) {
                loadModule(m, false, yangSearchPath, context);
            }
        }

        // 3. Build and attach a YangSchema so downstream callers that inspect
        //    context.getYangSchema() (e.g. YangYinParser.filter) work correctly.
        context.setYangSchema(buildYangSchema(moduleSets));

        return context;
    }

    // -------------------------------------------------------------------------
    // XML parsing helpers
    // -------------------------------------------------------------------------

    private static List<ParsedModuleSet> parseModuleSets(Element root) {
        List<ParsedModuleSet> result = new ArrayList<>();
        for (Object obj : root.elements()) {
            Element elem = (Element) obj;
            if ("module-set".equals(elem.getName())) {
                result.add(parseModuleSet(elem));
            }
        }
        return result;
    }

    private static ParsedModuleSet parseModuleSet(Element elem) {
        String name = childText(elem, "name");
        List<ParsedModule> modules = new ArrayList<>();
        List<ParsedModule> importOnlyModules = new ArrayList<>();

        for (Object obj : elem.elements()) {
            Element child = (Element) obj;
            switch (child.getName()) {
                case "module":
                    modules.add(parseModuleEntry(child));
                    break;
                case "import-only-module":
                    importOnlyModules.add(parseModuleEntry(child));
                    break;
                default:
                    break;
            }
        }
        return new ParsedModuleSet(name, modules, importOnlyModules);
    }

    private static ParsedModule parseModuleEntry(Element elem) {
        String name      = childText(elem, "name");
        String revision  = childText(elem, "revision");

        List<String> features   = new ArrayList<>();
        List<String> deviations = new ArrayList<>();
        List<String> locations  = new ArrayList<>();

        for (Object obj : elem.elements()) {
            Element child = (Element) obj;
            switch (child.getName()) {
                case "feature":
                    features.add(child.getTextTrim());
                    break;
                case "deviation":
                    deviations.add(child.getTextTrim());
                    break;
                case "location":
                    locations.add(child.getTextTrim());
                    break;
                default:
                    break;
            }
        }
        return new ParsedModule(name, revision, features, deviations, locations);
    }

    /** Returns trimmed text of the first matching child element, or {@code null}. */
    private static String childText(Element parent, String localName) {
        for (Object obj : parent.elements()) {
            Element child = (Element) obj;
            if (localName.equals(child.getName())) {
                String text = child.getTextTrim();
                return (text == null || text.isEmpty()) ? null : text;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // YangSchema / ModuleSet construction (for context filtering metadata)
    // -------------------------------------------------------------------------

    private static YangSchema buildYangSchema(List<ParsedModuleSet> moduleSets) {
        YangSchema yangSchema = new YangSchema();
        for (ParsedModuleSet ms : moduleSets) {
            ModuleSet moduleSet = new ModuleSet();
            moduleSet.setName(ms.name);
            for (ParsedModule m : ms.modules) {
                YangModuleDescription desc =
                        new YangModuleDescription(new ModuleId(m.name, m.revision));
                for (String feature : m.features) {
                    desc.addFeature(feature);
                }
                for (String deviation : m.deviations) {
                    desc.addDeviation(deviation);
                }
                moduleSet.addModule(desc);
            }
            for (ParsedModule m : ms.importOnlyModules) {
                YangModuleDescription desc =
                        new YangModuleDescription(new ModuleId(m.name, m.revision));
                moduleSet.addImportOnlyModule(desc);
            }
            yangSchema.addModuleSet(moduleSet);
        }
        return yangSchema;
    }

    // -------------------------------------------------------------------------
    // Module loading
    // -------------------------------------------------------------------------

    private static void loadModule(ParsedModule m, boolean importOnly,
                                   File searchPath, YangSchemaContext context)
            throws IOException, YangParserException, DocumentException {

        // Try explicit file:// locations listed in the yang-library XML first.
        for (String location : m.locations) {
            if (location.startsWith("file://")) {
                File f = new File(location.substring(7));
                if (f.exists()) {
                    logger.debug("Loading module '{}' from {}", m.name, f.getName());
                    try (FileInputStream fis = new FileInputStream(f)) {
                        YangYinParser.parse(fis, f.getAbsolutePath(), true, importOnly, context);
                    }
                    logger.debug("Done loading module '{}'", m.name);
                    return;
                }
            }
        }

        // Fall back to the caller-supplied search directory.
        if (searchPath != null) {
            File found = findYangFile(m.name, m.revision, searchPath);
            if (found != null) {
                logger.debug("Loading module '{}' from {}", m.name, found.getName());
                try (FileInputStream fis = new FileInputStream(found)) {
                    YangYinParser.parse(fis, found.getAbsolutePath(), true, importOnly, context);
                }
                logger.debug("Done loading module '{}'", m.name);
                return;
            }
        }

        throw new IOException(
                "Cannot locate YANG file for module '" + m.name + "'"
                        + (m.revision != null ? " revision " + m.revision : "")
                        + ". Tried explicit locations and search path: "
                        + searchPath);
    }

    /**
     * Searches {@code searchPath} for a file named {@code name@revision.yang},
     * then {@code name.yang}, then {@code name@revision.yin}, then {@code name.yin}.
     */
    private static File findYangFile(String name, String revision, File searchPath) {
        String[] candidates = (revision != null && !revision.isEmpty())
                ? new String[]{
                        name + "@" + revision + ".yang",
                        name + ".yang",
                        name + "@" + revision + ".yin",
                        name + ".yin"}
                : new String[]{
                        name + ".yang",
                        name + ".yin"};

        for (String candidate : candidates) {
            File f = new File(searchPath, candidate);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Internal data-transfer objects
    // -------------------------------------------------------------------------

    private static final class ParsedModuleSet {
        final String name;
        final List<ParsedModule> modules;
        final List<ParsedModule> importOnlyModules;

        ParsedModuleSet(String name,
                        List<ParsedModule> modules,
                        List<ParsedModule> importOnlyModules) {
            this.name = name;
            this.modules = modules;
            this.importOnlyModules = importOnlyModules;
        }
    }

    private static final class ParsedModule {
        final String name;
        final String revision;   // may be null
        final List<String> features;
        final List<String> deviations;
        final List<String> locations;

        ParsedModule(String name, String revision,
                     List<String> features, List<String> deviations,
                     List<String> locations) {
            this.name = name;
            this.revision = revision;
            this.features = features;
            this.deviations = deviations;
            this.locations = locations;
        }
    }
}
