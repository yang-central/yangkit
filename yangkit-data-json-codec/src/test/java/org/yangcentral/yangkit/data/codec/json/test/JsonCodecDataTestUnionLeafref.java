package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import org.yangcentral.yangkit.data.codec.json.JsonCodecUtil;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonCodecDataTestUnionLeafref {
    private YangSchemaContext parseModule(String moduleInfo, String yangText)
            throws IOException, YangParserException, DocumentException {
        return YangYinParser.parse(
                new ByteArrayInputStream(yangText.getBytes(StandardCharsets.UTF_8)),
                moduleInfo,
                null);
    }

    @Test
    public void validUnionLeafrefMemberShouldDeserializeAndValidate() throws Exception {
        String yang = String.join("\n",
                "module union-leafref-data {",
                "  yang-version 1.1;",
                "  namespace \"urn:test:union-leafref:data\";",
                "  prefix uld;",
                "",
                "  container holder {",
                "    leaf target {",
                "      type string;",
                "    }",
                "",
                "    leaf selector {",
                "      type union {",
                "        type int32;",
                "        type leafref {",
                "          path \"../target\";",
                "        }",
                "      }",
                "    }",
                "  }",
                "}");

        YangSchemaContext schemaContext = parseModule("union-leafref-data.yang", yang);
        ValidatorResult schemaResult = schemaContext.validate();
        assertTrue(schemaResult.isOk(), "schema should validate and resolve union member leafref references");

        Module module = schemaContext.getLatestModule("union-leafref-data").orElse(null);
        assertNotNull(module, "parsed test module should be available in schema context");
        Container holder = (Container) module.getDataDefChild("holder");
        assertNotNull(holder, "holder container should exist");
        Leaf targetLeaf = (Leaf) holder.getDataDefChild("target");
        Leaf selectorLeaf = (Leaf) holder.getDataDefChild("selector");
        assertNotNull(targetLeaf, "target leaf should exist");
        assertNotNull(selectorLeaf, "selector leaf should exist");

        Union union = (Union) selectorLeaf.getType().getRestriction();
        LeafRef unionLeafRef = null;
        for (Type memberType : union.getActualTypes()) {
            if (memberType.getRestriction() instanceof LeafRef) {
                unionLeafRef = (LeafRef) memberType.getRestriction();
                break;
            }
        }
        assertNotNull(unionLeafRef, "selector union should expose its leafref member type");
        assertNotNull(unionLeafRef.getReferencedNode(),
                "schema validation should resolve referencedNode before JSON deserialization starts");

        JsonNode json = new ObjectMapper().readTree(String.join("",
                "{",
                "  \"target\": \"alpha\",",
                "  \"selector\": \"alpha\"",
                "}"));

        ValidatorResultBuilder parseResultBuilder = new ValidatorResultBuilder();
        ContainerDataJsonCodec codec = new ContainerDataJsonCodec(holder);
        ContainerData document = codec.deserialize(json, parseResultBuilder);
        if (document != null) {
            parseResultBuilder.merge(JsonCodecUtil.buildChildrenData(document, json));
        }
        ValidatorResult parseResult = parseResultBuilder.build();

        assertNotNull(document, "valid union leafref payload should deserialize into a document");
        assertTrue(parseResult.isOk(),
                "valid union leafref payload should not fail with invalid-value after schema validation: " + parseResult);

        ValidatorResult validationResult = document.validate();
        assertTrue(validationResult.isOk(), "valid union leafref payload should pass document validation: " + validationResult);
    }
}





