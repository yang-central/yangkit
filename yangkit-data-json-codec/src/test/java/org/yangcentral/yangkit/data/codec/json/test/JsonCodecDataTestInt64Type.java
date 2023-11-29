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

public class JsonCodecDataTestInt32Type {

    @Test
    public void validInt32Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validint321.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void validInt32Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validint322.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void validInt32Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validint323.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void validInt32Test4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validint324.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void validInt32Test5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validint325.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/validrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint321.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint322.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint323.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint324.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint325.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint326.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint327.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint328.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint329.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
    public void invalidInt32Test10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidint3210.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int32/invalidrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int32/int32.yang").getFile();
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
