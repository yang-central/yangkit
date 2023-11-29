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

public class JsonCodecDataTestUint8Type {

    @Test
    public void validUint8Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validuint81.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void validUint8Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validuint82.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void validUint8Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validuint83.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void validRangeTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void validRangeTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void validRangeTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/validrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
    public void invalidUint8Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint81.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint82.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint83.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint84.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint85.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint86.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint87.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint88.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint89.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidUint8Test10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invaliduint810.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidRangeTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invalidrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidRangeTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invalidrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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

    @Test
    public void invalidRangeTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint8/invalidrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint8/uint8.yang").getFile();
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
