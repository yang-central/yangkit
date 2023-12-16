package org.yangcentral.yangkit.data.codec.json.test.type;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.data.codec.json.test.JsonCodecDataFunc;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestUint32Type {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/valid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }


    @Test
    public void validRangeTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/validrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validRangeTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/validrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validRangeTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/validrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid5.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid6.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid7.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid8.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid9.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid10.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest11() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid11.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest12() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid12.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest13() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalid13.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidRangeTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalidrange1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidRangeTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalidrange2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidRangeTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalidrange3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidRangeTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/uint32/invalidrange4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/uint32/uint32.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

}


