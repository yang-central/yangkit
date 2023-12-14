package org.yangcentral.yangkit.data.codec.json.test;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestEmptyType {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid5.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid6.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid7.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid8.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid9.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/empty/invalid10.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/empty/empty.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

}
