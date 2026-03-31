package org.yangcentral.yangkit.data.codec.cbor.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationOptionsCborCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;
    private static Container wrapperContainer;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsCborCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsCborCodecTest.class.getClassLoader()
                .getResource("anydata-validation/payload/yang").getFile());
        assertTrue(outerSchemaContext.validate().isOk());
        assertTrue(payloadSchemaContext.validate().isOk());
        for (Module module : outerSchemaContext.getModules()) {
            if ("outer-anydata".equals(module.getArgStr())) {
                wrapperContainer = (Container) module.getDataNodeChildren().get(0);
                break;
            }
        }
        assertNotNull(wrapperContainer);
    }

    private byte[] buildCbor() throws Exception {
        String json = "{"
                + "\"payload-holder\":{"
                + "\"payload-anydata:payload-root\":{"
                + "\"value\":\"abc\""
                + "}"
                + "}"
                + "}";
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        return new ObjectMapper(new CBORFactory()).writeValueAsBytes(jsonNode);
    }

    @Test
    public void deserializeWithoutOptionsKeepsUnknownAnydataPayloadEmpty() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        ContainerData containerData = codec.deserialize(buildCbor(), validator);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(0, anyDataData.getValue().getDataChildren().size());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() throws Exception {
        ContainerDataCborCodec codec = new ContainerDataCborCodec(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        ContainerData containerData = codec.deserialize(buildCbor(), validator, options);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(1, anyDataData.getValue().getDataChildren().size());
        assertEquals("value", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
    }
}



