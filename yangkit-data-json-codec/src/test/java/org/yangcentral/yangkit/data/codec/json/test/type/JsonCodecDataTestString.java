package org.yangcentral.yangkit.data.codec.json.test.type;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.data.codec.json.test.JsonCodecDataFunc;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestString {
    private static final String XSD_REGEX_YANG = "type/string/xml-schema-regex.yang";

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/valid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validPatternTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validpattern1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validPatternTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validpattern2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validPatternTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validpattern3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validModifierTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validmodifier1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validLengthTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validlength1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validLengthTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validlength2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validLengthTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validlength3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validAllTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validall1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validAllTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/validall2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid5.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid6.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalid7.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidPatternTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidpattern1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidPatternTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidpattern2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidModifierTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidmodifier1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidModifierTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidmodifier2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidLengthTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidlength1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidLengthTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidlength2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidAllTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidall1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void invalidAllTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/invalidall2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/string/string.yang").getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void validXmlSchemaIdentifierPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-valid-identifier.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidXmlSchemaIdentifierPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-invalid-identifier.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void validXmlSchemaBasicLatinPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-valid-basic-latin.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidXmlSchemaBasicLatinPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-invalid-basic-latin.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void validXmlSchemaAnchorLiteralPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-valid-anchor-literal.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidXmlSchemaAnchorLiteralPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-invalid-anchor-literal.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

    @Test
    public void validXmlSchemaInvertMatchPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-valid-invert-match.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidXmlSchemaInvertMatchPattern() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/string/xsd-invalid-invert-match.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource(XSD_REGEX_YANG).getFile();
        JsonCodecDataFunc.expectedError(jsonFile, yangFile);
    }

}
