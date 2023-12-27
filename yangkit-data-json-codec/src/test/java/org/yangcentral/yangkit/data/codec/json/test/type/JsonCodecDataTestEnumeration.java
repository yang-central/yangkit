package org.yangcentral.yangkit.data.codec.json.test.type;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.data.codec.json.test.JsonCodecDataFunc;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestEnumeration {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/valid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest4() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid4.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest5() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid5.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest6() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid6.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest7() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid7.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest8() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid8.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest9() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid9.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest10() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("type/enumeration/invalid10.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("type/enumeration/enumeration.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }


}
