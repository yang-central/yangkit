package org.yangcentral.yangkit.test.parser;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.impl.schema.YangSchemaContextImpl;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.register.YangStatementRegister;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (YangParserException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

    }
}
