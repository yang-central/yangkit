package org.yangcentral.yangkit.data.codec.xml.test.type;

import org.junit.jupiter.api.Test;

/**
 * String type XML codec tests.
 * Tests various string constraints including pattern, length, and modifiers.
 */
public class XmlCodecDataTestString {
    
    private static final String YANG_FILE = "type/string/string.yang";
    private static final String XSD_REGEX_YANG_FILE = "type/string/xml-schema-regex.yang";
    
    @Test
    public void validNormal() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/valid1.xml", YANG_FILE);
    }
    
    @Test
    public void validPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/valid2.xml", YANG_FILE);
    }
    
    @Test
    public void validLength() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/valid3.xml", YANG_FILE);
    }
    
    @Test
    public void validPatternMulti() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/valid4.xml", YANG_FILE);
    }
    
    @Test
    public void validPatternModifier() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/valid5.xml", YANG_FILE);
    }
    
    @Test
    public void invalidPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/invalid1.xml", YANG_FILE);
    }
    
    @Test
    public void invalidLength() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/invalid2.xml", YANG_FILE);
    }
    
    @Test
    public void invalidPatternModifier() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/invalid3.xml", YANG_FILE);
    }
    
    @Test
    public void invalidPatternMulti() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/invalid4.xml", YANG_FILE);
    }
    
    @Test
    public void simpleTest() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/simple.xml", "type/simple.yang");
    }

    @Test
    public void validXmlSchemaIdentifierPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/xsd-valid-identifier.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void invalidXmlSchemaIdentifierPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/xsd-invalid-identifier.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void validXmlSchemaBasicLatinPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/xsd-valid-basic-latin.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void invalidXmlSchemaBasicLatinPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/xsd-invalid-basic-latin.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void validXmlSchemaAnchorLiteralPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/xsd-valid-anchor-literal.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void invalidXmlSchemaAnchorLiteralPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/xsd-invalid-anchor-literal.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void validXmlSchemaInvertMatchPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/string/xsd-valid-invert-match.xml", XSD_REGEX_YANG_FILE);
    }

    @Test
    public void invalidXmlSchemaInvertMatchPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/string/xsd-invalid-invert-match.xml", XSD_REGEX_YANG_FILE);
    }
}
