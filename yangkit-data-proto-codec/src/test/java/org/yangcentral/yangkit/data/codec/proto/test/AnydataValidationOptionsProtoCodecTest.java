package org.yangcentral.yangkit.data.codec.proto.test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.proto.ProtoDescriptorManager;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationOptionsProtoCodecTest {
    private static final String OUTER_NS = "urn:test:outer-anydata";
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static YangSchemaContext outerSchemaContext;
    private static YangSchemaContext payloadSchemaContext;
    private static Container wrapperContainer;

    @BeforeAll
    static void setUp() throws IOException, YangParserException, org.dom4j.DocumentException {
        outerSchemaContext = YangYinParser.parse(AnydataValidationOptionsProtoCodecTest.class.getClassLoader()
                .getResource("anydata-validation/outer/yang").getFile());
        payloadSchemaContext = YangYinParser.parse(AnydataValidationOptionsProtoCodecTest.class.getClassLoader()
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
        ProtoDescriptorManager.getInstance().clearCache();
    }

    private DynamicMessage buildWrapperMessage() {
        Descriptors.Descriptor wrapperDescriptor = ProtoDescriptorManager.getInstance().getDescriptor(wrapperContainer);
        assertNotNull(wrapperDescriptor);
        Descriptors.FieldDescriptor payloadHolderField = wrapperDescriptor.findFieldByName("payload_holder");
        assertNotNull(payloadHolderField);

        DynamicMessage.Builder anydataBuilder = DynamicMessage.newBuilder(payloadHolderField.getMessageType());
        Descriptors.FieldDescriptor valueField = payloadHolderField.getMessageType().findFieldByName("value");
        assertNotNull(valueField);
        anydataBuilder.setField(valueField, "{\"payload-anydata:payload-root\":{\"value\":\"abc\"}}");

        DynamicMessage.Builder wrapperBuilder = DynamicMessage.newBuilder(wrapperDescriptor);
        wrapperBuilder.setField(payloadHolderField, anydataBuilder.build());
        return wrapperBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private ContainerData deserialize(DynamicMessage message, AnydataValidationOptions options) {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(wrapperContainer);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        if (options == null) {
            return (ContainerData) ((YangDataProtoCodec<Container, ContainerData>) codec).deserialize(message, validator);
        }
        return (ContainerData) ((YangDataProtoCodec<Container, ContainerData>) codec).deserialize(message, validator, options);
    }

    @Test
    public void deserializeWithoutOptionsKeepsUnknownAnydataPayloadEmpty() {
        ContainerData containerData = deserialize(buildWrapperMessage(), null);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(0, anyDataData.getValue().getDataChildren().size());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() {
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(PAYLOAD_HOLDER_QNAME, payloadSchemaContext);

        ContainerData containerData = deserialize(buildWrapperMessage(), options);
        assertNotNull(containerData);
        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(1, anyDataData.getValue().getDataChildren().size());
        assertEquals("value", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
    }
}



