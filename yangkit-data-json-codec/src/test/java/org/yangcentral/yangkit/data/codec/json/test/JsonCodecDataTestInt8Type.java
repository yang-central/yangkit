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

public class JsonCodecDataTestStringType {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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

    @Test
    public void validTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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

    @Test
    public void invalidStringTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidstring1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringPatternTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidpattern1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringPatternTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidpattern2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringModifierTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidmodifier1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringModifierTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidmodifier2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringLengthTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidlength1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringLengthTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidlength2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringAllTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidall1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

    @Test
    public void invalidStringAllTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidall2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
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
        assertFalse(validatorResult.isOk());

    }

}
