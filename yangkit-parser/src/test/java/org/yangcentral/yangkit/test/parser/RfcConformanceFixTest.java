package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.jaxen.saxpath.Axis;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.restriction.YangString;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangAbsoluteLocationPathImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathFactory;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RfcConformanceFixTest {
    private YangSchemaContext parseModule(String moduleInfo, String yangText)
            throws IOException, YangParserException, DocumentException {
        return YangYinParser.parse(
                new ByteArrayInputStream(yangText.getBytes(StandardCharsets.UTF_8)),
                moduleInfo,
                null);
    }

    private YangAbsoluteLocationPath parseAbsolutePath(String path) throws Exception {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String[] steps = normalized.split("/");
        YangXPathFactory factory = new YangXPathFactory();
        YangAbsoluteLocationPathImpl absoluteLocationPath = (YangAbsoluteLocationPathImpl) factory.createAbsoluteLocationPath();
        for (String step : steps) {
            String[] qnameParts = step.split(":", 2);
            String prefix = qnameParts.length == 2 ? qnameParts[0] : "";
            String localName = qnameParts.length == 2 ? qnameParts[1] : qnameParts[0];
            absoluteLocationPath.addStep(factory.createNameStep(Axis.CHILD, prefix, localName));
        }
        return absoluteLocationPath;
    }

    private YangAbsoluteLocationPath parseXPathAbsolutePath(String path) throws Exception {
        YangXPath xpath = new YangXPathImpl(path);
        assertTrue(xpath.getRootExpr() instanceof YangAbsoluteLocationPath,
                "path should parse as an absolute instance-identifier");
        return (YangAbsoluteLocationPath) xpath.getRootExpr();
    }

    @Test
    public void leafrefRequireInstanceMustBeRejectedInYang10() throws Exception {
        String yang = String.join("\n",
                "module leafref-v10 {",
                "  namespace \"urn:test:leafref:v10\";",
                "  prefix lv10;",
                "",
                "  leaf target {",
                "    type string;",
                "  }",
                "",
                "  leaf ref-leaf {",
                "    type leafref {",
                "      path \"/lv10:target\";",
                "      require-instance false;",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("leafref-v10.yang", yang);
        assertFalse(schemaContext.validate().isOk(),
                "YANG 1.0 leafref must not accept require-instance");
    }

    @Test
    public void leafrefRequireInstanceMustBeAcceptedInYang11() throws Exception {
        String yang = String.join("\n",
                "module leafref-v11 {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:leafref:v11\";",
                "  prefix lv11;",
                "",
                "  leaf target {",
                "    type string;",
                "  }",
                "",
                "  leaf ref-leaf {",
                "    type leafref {",
                "      path \"/lv11:target\";",
                "      require-instance false;",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("leafref-v11.yang", yang);
        assertTrue(schemaContext.validate().isOk(),
                "YANG 1.1 leafref should accept require-instance");
    }

    @Test
    public void instanceIdentifierEvaluationHonorsRequireInstance() throws Exception {
        String yang = String.join("\n",
                "module instance-id-test {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:instance-id\";",
                "  prefix iit;",
                "",
                "  leaf target {",
                "    type string;",
                "  }",
                "",
                "  leaf strict-id {",
                "    type instance-identifier;",
                "  }",
                "",
                "  leaf lax-id {",
                "    type instance-identifier {",
                "      require-instance false;",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("instance-id-test.yang", yang);
        assertTrue(schemaContext.validate().isOk(), "instance-identifier test module should validate");

        Module module = schemaContext.getLatestModule("instance-id-test").orElse(null);
        assertNotNull(module);

        Leaf strictLeaf = (Leaf) module.getDataDefChild("strict-id");
        Leaf laxLeaf = (Leaf) module.getDataDefChild("lax-id");
        assertNotNull(strictLeaf);
        assertNotNull(laxLeaf);

        InstanceIdentifier strictRestriction = (InstanceIdentifier) strictLeaf.getType().getRestriction();
        InstanceIdentifier laxRestriction = (InstanceIdentifier) laxLeaf.getType().getRestriction();

        assertTrue(strictRestriction.evaluate(parseAbsolutePath("/iit:target")));
        assertFalse(strictRestriction.evaluate(parseAbsolutePath("/iit:missing")));
        assertTrue(laxRestriction.evaluate(parseAbsolutePath("/iit:missing")));
    }

    @Test
    public void instanceIdentifierShouldRequireCompleteKeyPredicates() throws Exception {
        String yang = String.join("\n",
                "module instance-id-list {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:instance-id:list\";",
                "  prefix iil;",
                "",
                "  list items {",
                "    key \"id\";",
                "    leaf id {",
                "      type string;",
                "    }",
                "    leaf value {",
                "      type string;",
                "    }",
                "  }",
                "",
                "  leaf strict-id {",
                "    type instance-identifier;",
                "  }",
                "",
                "  leaf lax-id {",
                "    type instance-identifier {",
                "      require-instance false;",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("instance-id-list.yang", yang);
        assertTrue(schemaContext.validate().isOk(), "instance-identifier keyed-list module should validate");

        Module module = schemaContext.getLatestModule("instance-id-list").orElse(null);
        assertNotNull(module);

        Leaf strictLeaf = (Leaf) module.getDataDefChild("strict-id");
        Leaf laxLeaf = (Leaf) module.getDataDefChild("lax-id");
        assertNotNull(strictLeaf);
        assertNotNull(laxLeaf);

        InstanceIdentifier strictRestriction = (InstanceIdentifier) strictLeaf.getType().getRestriction();
        InstanceIdentifier laxRestriction = (InstanceIdentifier) laxLeaf.getType().getRestriction();

        YangAbsoluteLocationPath valid = parseXPathAbsolutePath("/iil:items[iil:id='alpha']/iil:value");
        YangAbsoluteLocationPath missingKeyPredicate = parseXPathAbsolutePath("/iil:items/iil:value");
        YangAbsoluteLocationPath nonKeyPredicate = parseXPathAbsolutePath("/iil:items[iil:value='alpha']/iil:value");

        assertTrue(((YangAbsoluteLocationPathImpl) valid).isInstanceIdentifier(schemaContext));
        assertFalse(((YangAbsoluteLocationPathImpl) missingKeyPredicate).isInstanceIdentifier(schemaContext));
        assertFalse(((YangAbsoluteLocationPathImpl) nonKeyPredicate).isInstanceIdentifier(schemaContext));

        assertTrue(strictRestriction.evaluate(valid));
        assertFalse(strictRestriction.evaluate(missingKeyPredicate));
        assertFalse(strictRestriction.evaluate(nonKeyPredicate));
        assertFalse(laxRestriction.evaluate(missingKeyPredicate),
                "missing key predicates make the instance-identifier invalid even when require-instance=false");
        assertTrue(laxRestriction.evaluate(parseAbsolutePath("/iil:missing")),
                "require-instance=false should still allow unresolved but syntactically valid absolute paths");
    }

    @Test
    public void stringPatternShouldUseXmlSchemaRegexSemantics() throws Exception {
        String yang = String.join("\n",
                "module xsd-pattern-test {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:xsd-pattern\";",
                "  prefix xpt;",
                "",
                "  leaf identifier {",
                "    type string {",
                "      pattern \"\\\\i\\\\c*\";",
                "    }",
                "  }",
                "",
                "  leaf basic-latin {",
                "    type string {",
                "      pattern \"\\\\p{IsBasicLatin}+\";",
                "    }",
                "  }",
                "",
                "  leaf anchor-literal {",
                "    type string {",
                "      pattern \"^a$\";",
                "    }",
                "  }",
                "",
                "  leaf non-identifier {",
                "    type string {",
                "      pattern \"\\\\i\\\\c*\" {",
                "        modifier invert-match;",
                "      }",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("xsd-pattern-test.yang", yang);
        assertTrue(schemaContext.validate().isOk(),
                "XML Schema regex syntax should be accepted for YANG string patterns");

        Module module = schemaContext.getLatestModule("xsd-pattern-test").orElse(null);
        assertNotNull(module);

        Leaf identifierLeaf = (Leaf) module.getDataDefChild("identifier");
        Leaf basicLatinLeaf = (Leaf) module.getDataDefChild("basic-latin");
        Leaf anchorLiteralLeaf = (Leaf) module.getDataDefChild("anchor-literal");
        Leaf nonIdentifierLeaf = (Leaf) module.getDataDefChild("non-identifier");
        assertNotNull(identifierLeaf);
        assertNotNull(basicLatinLeaf);
        assertNotNull(anchorLiteralLeaf);
        assertNotNull(nonIdentifierLeaf);

        YangString identifierRestriction = (YangString) identifierLeaf.getType().getRestriction();
        YangString basicLatinRestriction = (YangString) basicLatinLeaf.getType().getRestriction();
        YangString anchorLiteralRestriction = (YangString) anchorLiteralLeaf.getType().getRestriction();
        YangString nonIdentifierRestriction = (YangString) nonIdentifierLeaf.getType().getRestriction();

        assertTrue(identifierRestriction.evaluate("node1"));
        assertFalse(identifierRestriction.evaluate("1node"));
        assertTrue(basicLatinRestriction.evaluate("ABC123"));
        assertFalse(basicLatinRestriction.evaluate("中文"));
        assertTrue(anchorLiteralRestriction.evaluate("^a$"));
        assertFalse(anchorLiteralRestriction.evaluate("a"));
        assertFalse(nonIdentifierRestriction.evaluate("node1"));
        assertTrue(nonIdentifierRestriction.evaluate("1node"));
    }

    @Test
    public void xpathMustWhenShouldAcceptKeySchemaAxes() throws Exception {
        String yang = String.join("\n",
                "module xpath-axis-positive {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:xpath:axis:positive\";",
                "  prefix xpp;",
                "",
                "  container parent {",
                "    leaf flag {",
                "      type string;",
                "    }",
                "",
                "    leaf first {",
                "      type string;",
                "      must \"following-sibling::second\";",
                "    }",
                "",
                "    leaf second {",
                "      type string;",
                "      must \"preceding-sibling::first\";",
                "    }",
                "",
                "    must \"descendant::value\";",
                "",
                "    container child {",
                "      leaf value {",
                "        when \"ancestor-or-self::child\";",
                "        type string;",
                "        must \"ancestor::parent/flag\";",
                "      }",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("xpath-axis-positive.yang", yang);
        assertTrue(schemaContext.validate().isOk(),
                "must/when should accept ancestor/descendant/sibling schema axes after the fix");
    }

    @Test
    public void xpathValidatorShouldStillRejectUnsupportedAxes() throws Exception {
        String yang = String.join("\n",
                "module xpath-axis-negative {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:xpath:axis:negative\";",
                "  prefix xpn;",
                "",
                "  container top {",
                "    leaf a {",
                "      type string;",
                "      must \"following::b\";",
                "    }",
                "",
                "    leaf b {",
                "      type string;",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("xpath-axis-negative.yang", yang);
        ValidatorResult result = schemaContext.validate();
        assertTrue(result.toString().contains(ErrorCode.INVALID_XPATH.getFieldName()),
                "unsupported XPath axes should still emit INVALID_XPATH diagnostics");
    }
}



