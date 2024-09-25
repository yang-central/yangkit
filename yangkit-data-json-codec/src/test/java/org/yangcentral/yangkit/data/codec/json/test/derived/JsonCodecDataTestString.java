package org.yangcentral.yangkit.data.codec.json.test.derived;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.data.codec.json.test.JsonCodecDataFunc;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestString {

    @Test
    public void validTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/valid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/valid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }

    @Test
    public void validTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/valid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedNoError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/invalid1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/invalid2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest2bis() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/invalid2bis.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

    @Test
    public void invalidTest3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/invalid3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }
    @Test
    public void invalidTest3bis() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("derived/string/invalid3bis.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("derived/string/string.yang").getFile();
        JsonCodecDataFunc.expectedBadElementError(jsonFile, yangFile);
    }

}
