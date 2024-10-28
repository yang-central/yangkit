package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonCodecDataFunc {

    public static void expectedNoError(String jsonFile, String yangFile) throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        } catch (IOException ignored) {
        }

        assertNotEquals(jsonNode, null);

        ValidatorResult validatorResult = schemaContext.validate();
        assertTrue(validatorResult.isOk(), "yang schema is not valid");

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataDocumentJsonParser(schemaContext).parse(jsonNode,validatorResultBuilder);
        assertTrue(validatorResultBuilder.build().isOk(), "error during first validation of json");

        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        assertTrue(validatorResult.isOk(), "error during second validation of json");
    }

    public static void expectedBadElementError(String jsonFile, String yangFile) throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        } catch (IOException ignored) {
        }

        assertNotEquals(jsonNode, null);

        ValidatorResult validatorResult = schemaContext.validate();
        assertTrue(validatorResult.isOk(), "yang schema is not valid");

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataDocumentJsonParser(schemaContext).parse(jsonNode,validatorResultBuilder);
        ValidatorResult parseResult = validatorResultBuilder.build();
        assertFalse(parseResult.isOk(), "no error during first validation of json");
        for (ValidatorRecord record : parseResult.getRecords()) {
            assertEquals("bad-element", record.getErrorTag().getName(),
                    "expected only bad-element error during first validation of json");
        }

        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        assertTrue(validatorResult.isOk(), "error during second validation of json");
    }

    public static void expectedBadElementJsonPathError(String jsonFile, String yangFile, String jsonPath) throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        } catch (IOException ignored) {
        }

        assertNotEquals(jsonNode, null);

        ValidatorResult validatorResult = schemaContext.validate();
        assertTrue(validatorResult.isOk(), "yang schema is not valid");

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataDocumentJsonParser(schemaContext).parse(jsonNode,validatorResultBuilder);
        ValidatorResult parseResult = validatorResultBuilder.build();
        assertFalse(parseResult.isOk(), "no error during first validation of json");
        for (ValidatorRecord record : parseResult.getRecords()) {
            assertEquals("bad-element", record.getErrorTag().getName(),
                    "expected only bad-element error during first validation of json");
            System.out.println(record.getErrorPath().toString());
            assertTrue(record.getErrorPath().toString().startsWith(jsonPath),
                    "expected json path during first validation of json is not equal to actual json path");

        }

        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        assertTrue(validatorResult.isOk(), "error during second validation of json");
    }
}
