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
        JsonCodecDataFunc.expectedBadElementJsonPathError(jsonFile, yangFile, "/normal");
    }

}
