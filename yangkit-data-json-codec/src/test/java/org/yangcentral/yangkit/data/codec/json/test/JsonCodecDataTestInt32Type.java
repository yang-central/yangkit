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

public class JsonCodecDataTestInt16Type {

    @Test
    public void validInt16Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validint161.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void validInt16Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validint162.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void validInt16Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validint163.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void validInt16Test4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validint164.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void validInt16Test5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validint165.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/validrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidInt16Test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidint161.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidInt16Test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidint162.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidInt16Test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidint163.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidInt16Test4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidint164.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidRangeTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidRangeTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
    public void invalidRangeTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/int16/invalidrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/int16/int16.yang").getFile();
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
