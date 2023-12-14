package org.yangcentral.yangkit.data.codec.json.test;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestBooleanType {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid5.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid6.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid7.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid8.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid9.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid10.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest11() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/boolean/invalid11.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/boolean/boolean.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

}
