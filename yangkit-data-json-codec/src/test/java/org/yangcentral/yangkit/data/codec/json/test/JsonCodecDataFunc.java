package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
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
        }catch (IOException ignored){}

        assertNotEquals(jsonNode, null);

        ValidatorResult validatorResult = schemaContext.validate();
        assertTrue(validatorResult.isOk());

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);
        assertTrue(validatorResultBuilder.build().isOk());

        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        assertTrue(validatorResult.isOk());
    }

    public static void expectedBadElementError(String jsonFile, String yangFile) throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        }catch (IOException ignored){}

        assertNotEquals(jsonNode, null);

        ValidatorResult validatorResult = schemaContext.validate();
        assertTrue(validatorResult.isOk());

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);
        ValidatorResult parseResult = validatorResultBuilder.build();
        assertFalse(parseResult.isOk());
        assertEquals(parseResult.getRecords().size(), 1);
        assertEquals(parseResult.getRecords().get(0).getErrorTag().getName(), "bad-element");

        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        assertTrue(validatorResult.isOk());
    }
}
