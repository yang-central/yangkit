package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.jaxen.saxpath.Axis;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
import org.yangcentral.yangkit.xpath.impl.YangAbsoluteLocationPathImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathFactory;

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



