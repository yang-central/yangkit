package org.yangcentral.yangkit.xpath.impl.function;

import org.jaxen.FunctionCallException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReMatchFunctionTest {
    private final ReMatchFunction function = new ReMatchFunction();

    @Test
    public void shouldMatchXmlSchemaRegexIdentifiers() throws Exception {
        assertTrue((Boolean) function.call(null, Arrays.asList("node1", "\\i\\c*")));
        assertFalse((Boolean) function.call(null, Arrays.asList("1node", "\\i\\c*")));
    }

    @Test
    public void shouldSupportXmlSchemaUnicodeBlockNames() throws Exception {
        assertTrue((Boolean) function.call(null, Arrays.asList("ABC123", "\\p{IsBasicLatin}+")));
        assertFalse((Boolean) function.call(null, Arrays.asList("中文", "\\p{IsBasicLatin}+")));
    }

    @Test
    public void shouldTreatAnchorsAsXmlSchemaLiterals() throws Exception {
        assertTrue((Boolean) function.call(null, Arrays.asList("^a$", "^a$")));
        assertFalse((Boolean) function.call(null, Arrays.asList("a", "^a$")));
    }

    @Test
    public void shouldRejectInvalidXmlSchemaRegexPatterns() {
        assertThrows(FunctionCallException.class,
                () -> function.call(null, Arrays.asList("node1", "[abc")));
    }
}


