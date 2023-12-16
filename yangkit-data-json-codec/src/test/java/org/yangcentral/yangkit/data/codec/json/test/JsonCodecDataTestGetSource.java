package org.yangcentral.yangkit.data.codec.json.test.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;


public class JsonCodecDataTestGetSource {

    @Test
    public void getJsonDoc() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("source/data.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("source/simple.yang").getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        }catch (IOException ignored){}
        assert jsonNode != null;

        schemaContext.validate();
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);

        // YangDataDocumentJsonCodec codec = new YangDataDocumentJsonCodec(yangDataDocument.getSchemaContext());
        // String result = codec.serialize(yangDataDocument).toString();
        String result = yangDataDocument.getDocString();
        assertTrue(result.equals(jsonNode.toString()) || result.equals(jsonNode.get("data").toString()));
    }

    @Test
    public void getYangModule() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("source/data.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("source/simple.yang").getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        }catch (IOException ignored){}

        schemaContext.validate();
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);

        String[] result = yangDataDocument.getModulesStrings();
        assertEquals(result.length, 1);
        assertArrayEquals(result, new String[]{readFile(yangFile)});
    }
    @Test
    public void getMultipleYangModules() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("source/data.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("source/yangs").getFile();
        String yangSimpleFile = this.getClass().getClassLoader().getResource("source/yangs/insa-test-simple.yang").getFile();
        String yangComplexFile = this.getClass().getClassLoader().getResource("source/yangs/insa-test-complex.yang").getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(new File(jsonFile));
        }catch (IOException ignored){}

        schemaContext.validate();
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);

        String[] result = yangDataDocument.getModulesStrings();
        assertEquals(result.length, 2);
        assertArrayEquals(result, new String[]{readFile(yangComplexFile), readFile(yangSimpleFile)});

    }

    public String readFile(String path){
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                contentBuilder.append(currentLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

}
