package org.yangcentral.yangkit.data.codec.xml.test.type;

import org.junit.jupiter.api.Test;

/**
 * UInt8 type XML codec tests.
 * Tests uint8 range constraints and boundary values.
 */
public class XmlCodecDataTestUint8 {
    
    private static final String YANG_FILE = "type/uint8/uint8.yang";
    
    @Test
    public void validNormal() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/uint8/valid1.xml", YANG_FILE);
    }
    
    @Test
    public void validRanged() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/uint8/valid2.xml", YANG_FILE);
    }
    
    @Test
    public void validBoundaryZero() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/uint8/valid3.xml", YANG_FILE);
    }
    
    @Test
    public void validBoundaryMax() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError("type/uint8/valid4.xml", YANG_FILE);
    }
    
    @Test
    public void invalidOverflow() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/uint8/invalid1.xml", YANG_FILE);
    }
    
    @Test
    public void invalidRange() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/uint8/invalid2.xml", YANG_FILE);
    }
    
    @Test
    public void invalidNegative() throws Exception {
        XmlCodecTypeTestFunc.expectedError("type/uint8/invalid3.xml", YANG_FILE);
    }
}
