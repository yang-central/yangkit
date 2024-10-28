package org.yangcentral.yangkit.data.codec.json.test;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

public class JsonCodecDataTestErrorPath {

    @Test
    public void test1() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("errorPath/test1.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("errorPath/test1.yang").getFile();
        JsonCodecDataFunc.expectedBadElementJsonPathError(jsonFile, yangFile, "/insa-test:insa-container/a/b/c/d/e");
    }

    @Test
    public void test2() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("errorPath/test2.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("errorPath/test2.yang").getFile();
        JsonCodecDataFunc.expectedBadElementJsonPathError(jsonFile, yangFile, "/insa-test:insa-container/a/b/c/d/e");
    }

    @Test
    public void test3() throws DocumentException, IOException, YangParserException {
        String jsonFile = this.getClass().getClassLoader().getResource("errorPath/test3.json").getFile();
        String yangFile = this.getClass().getClassLoader().getResource("errorPath/test3.yang").getFile();
        JsonCodecDataFunc.expectedBadElementJsonPathError(jsonFile, yangFile, "/insa-test:network/nodes/0/interfaces/interface/0/interface-type");
    }

}
