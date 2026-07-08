package org.yangcentral.yangkit.test.parser;

import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class EnumWhenTest {

    @Test
    public void testEnumWhenShouldReportInvalidSubstatement() throws Exception {
        InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("enum-when-test.yang");
        assertNotNull(input, "Test YANG file should exist");

        System.out.println("=== Parsing YANG file ===");
        YangSchemaContext schemaContext = YangYinParser.parse(input, "enum-when-test.yang", null);

        System.out.println("=== Validating ===");
        ValidatorResult result = schemaContext.validate();

        System.out.println("=== Result ===");
        System.out.println("isOk: " + result.isOk());
        System.out.println("records: " + result.getRecords());

        // Should NOT be ok - 'when' under 'enum' is invalid per RFC 7950
        assertFalse(result.isOk(), "Expected validation failure for 'when' under 'enum'. Records: " + result.getRecords());
    }
}
