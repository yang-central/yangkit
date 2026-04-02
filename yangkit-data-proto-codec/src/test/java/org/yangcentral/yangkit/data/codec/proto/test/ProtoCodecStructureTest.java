package org.yangcentral.yangkit.data.codec.proto.test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode;
import org.yangcentral.yangkit.data.codec.proto.ProtoDescriptorManager;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.dom4j.DocumentException;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sx:structure resolution in protobuf codec.
 *
 * <p>The YANG {@code sx:structure} extension (RFC 8791) defines data structures
 * outside the normal YANG config/state data tree. This test validates
 * that the protobuf codec correctly handles these structures.</p>
 *
 * @see ProtoCodecDataTest
 */
public class ProtoCodecStructureTest {

    private static YangSchemaContext schemaContext;
    private static YangStructure testStructure;
    private static Container metadataContainer;
    private static Container payloadContainer;

    @BeforeAll
    static void setUp() throws DocumentException, IOException, YangParserException {
        String yangDir = ProtoCodecStructureTest.class.getClassLoader()
                .getResource("proto/yang")
                .getFile();
        schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();

        if (!result.isOk()) {
            System.out.println("YANG validation failed in StructureTest:");
            if (result.getRecords() != null) {
                result.getRecords().forEach(r -> 
                    System.out.println("  - " + r.getSeverity() + ": " + r.toString()));
            }
        }
        assertTrue(result.isOk(), "YANG modules should parse without errors");

        org.yangcentral.yangkit.model.api.stmt.Module structureModule = null;
        for (org.yangcentral.yangkit.model.api.stmt.Module module : schemaContext.getModules()) {
            if ("test-proto-structure".equals(module.getArgStr())) {
                structureModule = module;
                break;
            }
        }

        assertNotNull(structureModule, "test-proto-structure module should be present in proto test resources");

        for (SchemaNode child : structureModule.getSchemaNodeChildren()) {
            if (child instanceof YangStructure && "message".equals(child.getArgStr())) {
                testStructure = (YangStructure) child;
                break;
            }
        }

        assertNotNull(testStructure, "sx:structure message should be discovered from module schema-node children");

        for (Object child : ((org.yangcentral.yangkit.model.api.stmt.DataDefContainer) testStructure).getDataDefChildren()) {
            if (child instanceof Container && "metadata".equals(((Container) child).getArgStr())) {
                metadataContainer = (Container) child;
            } else if (child instanceof Container && "payload".equals(((Container) child).getArgStr())) {
                payloadContainer = (Container) child;
            }
        }

        assertNotNull(metadataContainer, "structure should contain a metadata container");
        assertNotNull(payloadContainer, "structure should contain a payload container");
    }

    /**
     * Verifies that RFC 8791 sx:structure is really discovered by the parser/model layer.
     */
    @Test
    public void testStructureCodec() {
        assertNotNull(testStructure, "sx:structure must be resolved for protobuf structure tests");
        assertEquals("message", testStructure.getArgStr());
        assertNotNull(testStructure.getIdentifier());

        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(testStructure);
        assertNotNull(codec, "Factory should return a protobuf codec instance for YangStructure");
    }

    /**
     * Verifies that nested containers inside the structure are available to the protobuf codec layer.
     */
    @Test
    public void testNestedContainerInStructure() {
        assertNotNull(metadataContainer, "metadata container should be present inside the structure");
        assertEquals("metadata", metadataContainer.getArgStr());
        boolean hasTimestampLeaf = metadataContainer.getDataDefChildren().stream()
                .anyMatch(child -> "timestamp".equals(child.getArgStr()));
        assertTrue(hasTimestampLeaf, "metadata container should expose the timestamp leaf");

        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(metadataContainer);
        assertNotNull(codec, "Factory should return a protobuf codec instance for nested structure containers");
    }

    @Test
    public void testStructureDescriptorInSimpleAndYgotModes() {
        Descriptors.Descriptor simpleDesc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(testStructure);
        Descriptors.Descriptor ygotDesc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(testStructure);

        assertNotNull(simpleDesc, "SIMPLE mode should build a descriptor for YangStructure");
        assertNotNull(ygotDesc, "YGOT mode should build a descriptor for YangStructure");

        Descriptors.FieldDescriptor simpleMetadata = simpleDesc.findFieldByName("metadata");
        Descriptors.FieldDescriptor simplePayload = simpleDesc.findFieldByName("payload");
        assertNotNull(simpleMetadata, "Structure descriptor should expose metadata field");
        assertNotNull(simplePayload, "Structure descriptor should expose payload field");

        Descriptors.Descriptor simpleMetadataDesc = simpleMetadata.getMessageType();
        assertNotNull(simpleMetadataDesc.findFieldByName("timestamp"));
        assertNotNull(simpleMetadataDesc.findFieldByName("source"));
        assertNotNull(simpleMetadataDesc.findFieldByName("sequence_number"));

        Descriptors.FieldDescriptor ygotMetadata = ygotDesc.findFieldByName("metadata");
        assertNotNull(ygotMetadata, "YGOT descriptor should expose metadata field");
        Descriptors.Descriptor ygotMetadataDesc = ygotMetadata.getMessageType();
        Descriptors.FieldDescriptor ygotTimestamp = ygotMetadataDesc.findFieldByName("timestamp");
        Descriptors.FieldDescriptor ygotSequence = ygotMetadataDesc.findFieldByName("sequence_number");
        assertNotNull(ygotTimestamp, "YGOT metadata descriptor should expose timestamp field");
        assertNotNull(ygotSequence, "YGOT metadata descriptor should expose sequence_number field");
        assertEquals(Descriptors.FieldDescriptor.Type.MESSAGE, ygotTimestamp.getType(),
                "YGOT timestamp should be represented by a wrapper message");
        assertEquals(Descriptors.FieldDescriptor.Type.MESSAGE, ygotSequence.getType(),
                "YGOT sequence_number should be represented by a wrapper message");
    }

    @Test
    public void testStructureRoundTripInSimpleMode() {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(testStructure, ProtoCodecMode.SIMPLE);
        DynamicMessage input = buildSimpleStructureMessage();

        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangStructureData structureData = (YangStructureData) codec.deserialize(input, validatorBuilder);

        assertNotNull(structureData, "SIMPLE mode should deserialize structure data");
        assertTrue(validatorBuilder.build().isOk(), "SIMPLE mode structure payload should not add validation errors");

        ContainerData metadata = getRequiredContainer(structureData, "metadata");
        ContainerData payload = getRequiredContainer(structureData, "payload");
        assertLeafString(metadata, "timestamp", "2026-04-02T11:15:00Z");
        assertLeafString(metadata, "source", "sensor-a");
        assertLeafString(metadata, "sequence-number", "7");
        assertLeafString(payload, "data-type", "json");
        assertLeafString(payload, "content", "payload-1");

        DynamicMessage serialized = codec.serialize(structureData);
        assertNotNull(serialized, "SIMPLE mode should serialize structure data back to protobuf");

        Descriptors.FieldDescriptor metadataField = serialized.getDescriptorForType().findFieldByName("metadata");
        Descriptors.FieldDescriptor payloadField = serialized.getDescriptorForType().findFieldByName("payload");
        DynamicMessage metadataMessage = (DynamicMessage) serialized.getField(metadataField);
        DynamicMessage payloadMessage = (DynamicMessage) serialized.getField(payloadField);
        assertEquals("2026-04-02T11:15:00Z", metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("timestamp")));
        assertEquals("sensor-a", metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("source")));
        assertEquals(7, metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("sequence_number")));
        assertEquals("json", payloadMessage.getField(payloadMessage.getDescriptorForType().findFieldByName("data_type")));
        assertEquals("payload-1", payloadMessage.getField(payloadMessage.getDescriptorForType().findFieldByName("content")));
    }

    @Test
    public void testStructureRoundTripInYgotMode() {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(testStructure, ProtoCodecMode.YGOT);
        DynamicMessage input = buildYgotStructureMessage();

        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangStructureData structureData = (YangStructureData) codec.deserialize(input, validatorBuilder);

        assertNotNull(structureData, "YGOT mode should deserialize structure data");
        assertTrue(validatorBuilder.build().isOk(), "YGOT mode structure payload should not add validation errors");

        ContainerData metadata = getRequiredContainer(structureData, "metadata");
        ContainerData payload = getRequiredContainer(structureData, "payload");
        assertLeafString(metadata, "timestamp", "2026-04-02T11:20:00Z");
        assertLeafString(metadata, "source", "sensor-b");
        assertLeafString(metadata, "sequence-number", "9");
        assertLeafString(payload, "data-type", "xml");
        assertLeafString(payload, "content", "payload-2");

        DynamicMessage serialized = codec.serialize(structureData);
        assertNotNull(serialized, "YGOT mode should serialize structure data back to protobuf");

        DynamicMessage metadataMessage = (DynamicMessage) serialized.getField(serialized.getDescriptorForType().findFieldByName("metadata"));
        DynamicMessage payloadMessage = (DynamicMessage) serialized.getField(serialized.getDescriptorForType().findFieldByName("payload"));
        assertEquals("2026-04-02T11:20:00Z", unwrapWrapperValue((DynamicMessage) metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("timestamp"))));
        assertEquals("sensor-b", unwrapWrapperValue((DynamicMessage) metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("source"))));
        assertEquals(9L, unwrapWrapperValue((DynamicMessage) metadataMessage.getField(metadataMessage.getDescriptorForType().findFieldByName("sequence_number"))));
        assertEquals("xml", unwrapWrapperValue((DynamicMessage) payloadMessage.getField(payloadMessage.getDescriptorForType().findFieldByName("data_type"))));
        assertEquals("payload-2", unwrapWrapperValue((DynamicMessage) payloadMessage.getField(payloadMessage.getDescriptorForType().findFieldByName("content"))));
    }

    private static DynamicMessage buildSimpleStructureMessage() {
        Descriptors.Descriptor structureDesc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(testStructure);
        assertNotNull(structureDesc, "SIMPLE structure descriptor should be available for test message creation");

        Descriptors.FieldDescriptor metadataField = structureDesc.findFieldByName("metadata");
        Descriptors.FieldDescriptor payloadField = structureDesc.findFieldByName("payload");
        DynamicMessage metadata = DynamicMessage.newBuilder(metadataField.getMessageType())
                .setField(metadataField.getMessageType().findFieldByName("timestamp"), "2026-04-02T11:15:00Z")
                .setField(metadataField.getMessageType().findFieldByName("source"), "sensor-a")
                .setField(metadataField.getMessageType().findFieldByName("sequence_number"), 7)
                .build();
        DynamicMessage payload = DynamicMessage.newBuilder(payloadField.getMessageType())
                .setField(payloadField.getMessageType().findFieldByName("data_type"), "json")
                .setField(payloadField.getMessageType().findFieldByName("content"), "payload-1")
                .build();

        return DynamicMessage.newBuilder(structureDesc)
                .setField(metadataField, metadata)
                .setField(payloadField, payload)
                .build();
    }

    private static DynamicMessage buildYgotStructureMessage() {
        Descriptors.Descriptor structureDesc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(testStructure);
        assertNotNull(structureDesc, "YGOT structure descriptor should be available for test message creation");

        Descriptors.FieldDescriptor metadataField = structureDesc.findFieldByName("metadata");
        Descriptors.FieldDescriptor payloadField = structureDesc.findFieldByName("payload");
        Descriptors.Descriptor metadataDesc = metadataField.getMessageType();
        Descriptors.Descriptor payloadDesc = payloadField.getMessageType();

        DynamicMessage metadata = DynamicMessage.newBuilder(metadataDesc)
                .setField(metadataDesc.findFieldByName("timestamp"), wrapValue(metadataDesc.findFieldByName("timestamp").getMessageType(), "2026-04-02T11:20:00Z"))
                .setField(metadataDesc.findFieldByName("source"), wrapValue(metadataDesc.findFieldByName("source").getMessageType(), "sensor-b"))
                .setField(metadataDesc.findFieldByName("sequence_number"), wrapValue(metadataDesc.findFieldByName("sequence_number").getMessageType(), 9L))
                .build();
        DynamicMessage payload = DynamicMessage.newBuilder(payloadDesc)
                .setField(payloadDesc.findFieldByName("data_type"), wrapValue(payloadDesc.findFieldByName("data_type").getMessageType(), "xml"))
                .setField(payloadDesc.findFieldByName("content"), wrapValue(payloadDesc.findFieldByName("content").getMessageType(), "payload-2"))
                .build();

        return DynamicMessage.newBuilder(structureDesc)
                .setField(metadataField, metadata)
                .setField(payloadField, payload)
                .build();
    }

    private static DynamicMessage wrapValue(Descriptors.Descriptor wrapperDesc, Object value) {
        Descriptors.FieldDescriptor valueField = wrapperDesc.findFieldByName("value");
        assertNotNull(valueField, "Wrapper descriptor should contain a value field");
        return DynamicMessage.newBuilder(wrapperDesc)
                .setField(valueField, value)
                .build();
    }

    private static Object unwrapWrapperValue(DynamicMessage wrapper) {
        Descriptors.FieldDescriptor valueField = wrapper.getDescriptorForType().findFieldByName("value");
        assertNotNull(valueField, "Wrapper message should contain a value field");
        return wrapper.getField(valueField);
    }

    private static ContainerData getRequiredContainer(YangStructureData structureData, String localName) {
        YangData<?> dataChild = structureData.getDataChildren().stream()
                .filter(child -> child.getSchemaNode() != null && localName.equals(child.getSchemaNode().getArgStr()))
                .findFirst()
                .orElse(null);
        assertNotNull(dataChild, "Expected structure child container: " + localName);
        assertTrue(dataChild instanceof ContainerData, "Expected child to be a ContainerData: " + localName);
        return (ContainerData) dataChild;
    }

    private static void assertLeafString(YangDataContainer container, String localName, String expectedValue) {
        YangData<?> dataChild = container.getDataChildren().stream()
                .filter(child -> child.getSchemaNode() != null && localName.equals(child.getSchemaNode().getArgStr()))
                .findFirst()
                .orElse(null);
        assertNotNull(dataChild, "Expected leaf child: " + localName);
        assertTrue(dataChild instanceof LeafData, "Expected leaf data for child: " + localName);
        assertEquals(expectedValue, ((LeafData<?>) dataChild).getStringValue());
    }
}
