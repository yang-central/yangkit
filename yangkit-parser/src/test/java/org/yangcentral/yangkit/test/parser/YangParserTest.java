package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.CapabilityParser;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yangcentral.yangkit.writter.YangFormatter;
import org.yangcentral.yangkit.writter.YangWriter;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class YangParserTest {

    @Test
    public void test_case_01() {

        try {
            URL url = this.getClass().getClassLoader().getResource("nullpointer-example.yang");
            InputStream inputStream = url.openStream();

            YangSchemaContext schemaContext = YangYinParser.parse(inputStream,"nullpointer-example.yang",null);
            ValidatorResult result = schemaContext.validate();
            assertTrue(result.isOk());
        } catch (IOException | YangParserException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void test_case_02(){
        try {
            //URL capabilitiesPath = this.getClass().getClassLoader().getResource("capabilities.xml");
            URL yangPath = this.getClass().getClassLoader().getResource("rj");
            //YangSchemaContext schemaContext = YangYinParser.parse(yangPath.getFile(),capabilitiesPath.getFile());
            YangSchemaContext schemaContext = YangYinParser.parse(yangPath.getFile());

            ValidatorResult validatorResult = schemaContext.validate();
            if(!validatorResult.isOk()){
                System.out.println(validatorResult);
            }
        } catch (IOException | YangParserException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
