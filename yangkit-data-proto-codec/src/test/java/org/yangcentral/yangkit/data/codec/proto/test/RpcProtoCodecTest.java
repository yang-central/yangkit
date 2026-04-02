package org.yangcentral.yangkit.data.codec.proto.test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.InputData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.OutPutData;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.codec.proto.InputDataProtoCodec;
import org.yangcentral.yangkit.data.codec.proto.OutputDataProtoCodec;
import org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode;
import org.yangcentral.yangkit.data.codec.proto.ProtoDescriptorManager;
import org.yangcentral.yangkit.data.codec.proto.RpcDataProtoCodec;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Input;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Output;
import org.yangcentral.yangkit.model.api.stmt.Rpc;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executable regression tests for protobuf RPC descriptor generation and
 * input/output round-trip behavior.
 */
public class RpcProtoCodecTest {

    private static YangSchemaContext schemaContext;
    private static Rpc getInfoRpc;
    private static Rpc updateConfigRpc;
    private static Input getInfoInput;
    private static Output getInfoOutput;
    private static Input updateConfigInput;
    private static Output updateConfigOutput;

    @BeforeAll
    static void setUp() throws DocumentException, IOException, YangParserException {
        String yangDir = Objects.requireNonNull(
                RpcProtoCodecTest.class.getClassLoader().getResource("proto/yang"),
                "proto/yang test resource directory should exist")
                .getFile();

        schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();
        assertTrue(result.isOk(), "YANG modules should parse without errors");

        Module rpcModule = schemaContext.getModules().stream()
                .filter(module -> "test-proto-rpc".equals(module.getArgStr()))
                .findFirst()
                .orElse(null);
        assertNotNull(rpcModule, "test-proto-rpc module should be present in proto test resources");

        getInfoRpc = rpcModule.getRpc("get-info");
        updateConfigRpc = rpcModule.getRpc("update-config");
        assertNotNull(getInfoRpc, "get-info RPC should be discoverable from the module");
        assertNotNull(updateConfigRpc, "update-config RPC should be discoverable from the module");

        getInfoInput = getInfoRpc.getInput();
        getInfoOutput = getInfoRpc.getOutput();
        updateConfigInput = updateConfigRpc.getInput();
        updateConfigOutput = updateConfigRpc.getOutput();
        assertNotNull(getInfoInput, "get-info RPC should expose input");
        assertNotNull(getInfoOutput, "get-info RPC should expose output");
        assertNotNull(updateConfigInput, "update-config RPC should expose input");
        assertNotNull(updateConfigOutput, "update-config RPC should expose output");
    }

    @Test
    public void testRpcDiscoveryAndCodecTypes() {
        YangDataProtoCodec<?, ?> rpcCodec = YangDataProtoCodec.getInstance(getInfoRpc);
        YangDataProtoCodec<?, ?> inputCodec = YangDataProtoCodec.getInstance(getInfoInput);
        YangDataProtoCodec<?, ?> outputCodec = YangDataProtoCodec.getInstance(getInfoOutput);

        assertNotNull(rpcCodec, "Factory should return a codec for Rpc nodes");
        assertNotNull(inputCodec, "Factory should return a codec for RPC input nodes");
        assertNotNull(outputCodec, "Factory should return a codec for RPC output nodes");
        assertTrue(rpcCodec instanceof RpcDataProtoCodec, "RPC codec should be RpcDataProtoCodec");
        assertTrue(inputCodec instanceof InputDataProtoCodec, "Input codec should be InputDataProtoCodec");
        assertTrue(outputCodec instanceof OutputDataProtoCodec, "Output codec should be OutputDataProtoCodec");
        assertNotSame(inputCodec.getClass(), outputCodec.getClass(),
                "Input and output codecs should be different concrete types");
    }

    @Test
    public void testRpcDescriptorsInSimpleAndYgotModes() {
        Descriptors.Descriptor rpcSimple = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoRpc);
        Descriptors.Descriptor rpcYgot = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(getInfoRpc);
        Descriptors.Descriptor inputSimple = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoInput);
        Descriptors.Descriptor inputYgot = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(getInfoInput);
        Descriptors.Descriptor outputSimple = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoOutput);
        Descriptors.Descriptor outputYgot = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(getInfoOutput);
        Descriptors.Descriptor updateInputSimple = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(updateConfigInput);

        assertNotNull(rpcSimple, "SIMPLE mode should build a descriptor for the RPC node");
        assertNotNull(rpcYgot, "YGOT mode should build a descriptor for the RPC node");
        assertNotNull(inputSimple, "SIMPLE mode should build a descriptor for RPC input");
        assertNotNull(inputYgot, "YGOT mode should build a descriptor for RPC input");
        assertNotNull(outputSimple, "SIMPLE mode should build a descriptor for RPC output");
        assertNotNull(outputYgot, "YGOT mode should build a descriptor for RPC output");
        assertNotNull(updateInputSimple, "Complex RPC input should also have a descriptor");

        assertNotNull(rpcSimple.findNestedTypeByName("GetInfoInput"),
                "RPC descriptor should contain nested GetInfoInput message");
        assertNotNull(rpcSimple.findNestedTypeByName("GetInfoOutput"),
                "RPC descriptor should contain nested GetInfoOutput message");

        assertNotNull(inputSimple.findFieldByName("request_id"));
        assertNotNull(inputSimple.findFieldByName("verbose"));
        assertEquals(Descriptors.FieldDescriptor.Type.STRING,
                inputSimple.findFieldByName("request_id").getType());
        assertEquals(Descriptors.FieldDescriptor.Type.BOOL,
                inputSimple.findFieldByName("verbose").getType());

        assertEquals(Descriptors.FieldDescriptor.Type.MESSAGE,
                inputYgot.findFieldByName("request_id").getType());
        assertEquals(Descriptors.FieldDescriptor.Type.MESSAGE,
                inputYgot.findFieldByName("verbose").getType());
        assertEquals(Descriptors.FieldDescriptor.Type.MESSAGE,
                outputYgot.findFieldByName("code").getType());

        Descriptors.FieldDescriptor configField = updateInputSimple.findFieldByName("config");
        assertNotNull(configField, "Complex RPC input should contain config container field");
        Descriptors.Descriptor configDesc = configField.getMessageType();
        assertNotNull(configDesc.findFieldByName("name"));
        assertNotNull(configDesc.findFieldByName("value"));
        assertNotNull(configDesc.findFieldByName("enabled"));
    }

    @Test
    public void testGetInfoInputRoundTripSimple() {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(getInfoInput, ProtoCodecMode.SIMPLE);
        DynamicMessage inputMessage = buildSimpleGetInfoInputMessage();

        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        InputData inputData = (InputData) codec.deserialize(inputMessage, validatorBuilder);
        assertNotNull(inputData, "SIMPLE get-info input should deserialize");
        assertTrue(validatorBuilder.build().isOk(), "SIMPLE get-info input should not add validation errors");
        assertLeafString(inputData, "request-id", "req-1");
        assertLeafString(inputData, "verbose", "true");

        DynamicMessage serialized = codec.serialize(inputData);
        assertNotNull(serialized, "SIMPLE get-info input should serialize back to protobuf");
        assertEquals("req-1", serialized.getField(serialized.getDescriptorForType().findFieldByName("request_id")));
        assertEquals(true, serialized.getField(serialized.getDescriptorForType().findFieldByName("verbose")));
    }

    @Test
    public void testGetInfoInputRoundTripYgot() {
        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(getInfoInput, ProtoCodecMode.YGOT);
        DynamicMessage inputMessage = buildYgotGetInfoInputMessage();

        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        InputData inputData = (InputData) codec.deserialize(inputMessage, validatorBuilder);
        assertNotNull(inputData, "YGOT get-info input should deserialize");
        assertTrue(validatorBuilder.build().isOk(), "YGOT get-info input should not add validation errors");
        assertLeafString(inputData, "request-id", "req-2");
        assertLeafString(inputData, "verbose", "false");

        DynamicMessage serialized = codec.serialize(inputData);
        assertNotNull(serialized, "YGOT get-info input should serialize back to protobuf");
        assertEquals("req-2", unwrapWrapperValue((DynamicMessage) serialized.getField(
                serialized.getDescriptorForType().findFieldByName("request_id"))));
        assertEquals(false, unwrapWrapperValue((DynamicMessage) serialized.getField(
                serialized.getDescriptorForType().findFieldByName("verbose"))));
    }

    @Test
    public void testGetInfoOutputRoundTripSimpleAndYgot() {
        YangDataProtoCodec<?, ?> simpleCodec = YangDataProtoCodec.getInstance(getInfoOutput, ProtoCodecMode.SIMPLE);
        YangDataProtoCodec<?, ?> ygotCodec = YangDataProtoCodec.getInstance(getInfoOutput, ProtoCodecMode.YGOT);

        DynamicMessage simpleOutputMessage = buildSimpleGetInfoOutputMessage();
        DynamicMessage ygotOutputMessage = buildYgotGetInfoOutputMessage();

        ValidatorResultBuilder simpleValidator = new ValidatorResultBuilder();
        OutPutData simpleOutputData = (OutPutData) simpleCodec.deserialize(simpleOutputMessage, simpleValidator);
        assertNotNull(simpleOutputData, "SIMPLE get-info output should deserialize");
        assertTrue(simpleValidator.build().isOk(), "SIMPLE get-info output should not add validation errors");
        assertLeafString(simpleOutputData, "response-id", "resp-1");
        assertLeafString(simpleOutputData, "status", "ok");
        assertLeafString(simpleOutputData, "code", "200");

        DynamicMessage simpleSerialized = simpleCodec.serialize(simpleOutputData);
        assertEquals("resp-1", simpleSerialized.getField(simpleSerialized.getDescriptorForType().findFieldByName("response_id")));
        assertEquals("ok", simpleSerialized.getField(simpleSerialized.getDescriptorForType().findFieldByName("status")));
        assertEquals(200, simpleSerialized.getField(simpleSerialized.getDescriptorForType().findFieldByName("code")));

        ValidatorResultBuilder ygotValidator = new ValidatorResultBuilder();
        OutPutData ygotOutputData = (OutPutData) ygotCodec.deserialize(ygotOutputMessage, ygotValidator);
        assertNotNull(ygotOutputData, "YGOT get-info output should deserialize");
        assertTrue(ygotValidator.build().isOk(), "YGOT get-info output should not add validation errors");
        assertLeafString(ygotOutputData, "response-id", "resp-2");
        assertLeafString(ygotOutputData, "status", "warning");
        assertLeafString(ygotOutputData, "code", "201");

        DynamicMessage ygotSerialized = ygotCodec.serialize(ygotOutputData);
        assertEquals("resp-2", unwrapWrapperValue((DynamicMessage) ygotSerialized.getField(
                ygotSerialized.getDescriptorForType().findFieldByName("response_id"))));
        assertEquals("warning", unwrapWrapperValue((DynamicMessage) ygotSerialized.getField(
                ygotSerialized.getDescriptorForType().findFieldByName("status"))));
        assertEquals(201L, unwrapWrapperValue((DynamicMessage) ygotSerialized.getField(
                ygotSerialized.getDescriptorForType().findFieldByName("code"))));
    }

    @Test
    public void testComplexRpcInputAndOutputRoundTripSimple() {
        YangDataProtoCodec<?, ?> inputCodec = YangDataProtoCodec.getInstance(updateConfigInput, ProtoCodecMode.SIMPLE);
        YangDataProtoCodec<?, ?> outputCodec = YangDataProtoCodec.getInstance(updateConfigOutput, ProtoCodecMode.SIMPLE);

        DynamicMessage inputMessage = buildSimpleUpdateConfigInputMessage();
        ValidatorResultBuilder inputValidator = new ValidatorResultBuilder();
        InputData inputData = (InputData) inputCodec.deserialize(inputMessage, inputValidator);
        assertNotNull(inputData, "Complex RPC input should deserialize");
        assertTrue(inputValidator.build().isOk(), "Complex RPC input should not add validation errors");

        ContainerData config = getRequiredContainer(inputData, "config");
        assertLeafString(config, "name", "feature-a");
        assertLeafString(config, "value", "enabled");
        assertLeafString(config, "enabled", "true");

        DynamicMessage serializedInput = inputCodec.serialize(inputData);
        DynamicMessage configMessage = (DynamicMessage) serializedInput.getField(
                serializedInput.getDescriptorForType().findFieldByName("config"));
        assertEquals("feature-a", configMessage.getField(configMessage.getDescriptorForType().findFieldByName("name")));
        assertEquals("enabled", configMessage.getField(configMessage.getDescriptorForType().findFieldByName("value")));
        assertEquals(true, configMessage.getField(configMessage.getDescriptorForType().findFieldByName("enabled")));

        DynamicMessage outputMessage = buildSimpleUpdateConfigOutputMessage();
        ValidatorResultBuilder outputValidator = new ValidatorResultBuilder();
        OutPutData outputData = (OutPutData) outputCodec.deserialize(outputMessage, outputValidator);
        assertNotNull(outputData, "Complex RPC output should deserialize");
        assertTrue(outputValidator.build().isOk(), "Complex RPC output should not add validation errors");
        assertLeafString(outputData, "success", "true");
        assertLeafString(outputData, "message", "updated");

        DynamicMessage serializedOutput = outputCodec.serialize(outputData);
        assertEquals(true, serializedOutput.getField(serializedOutput.getDescriptorForType().findFieldByName("success")));
        assertEquals("updated", serializedOutput.getField(serializedOutput.getDescriptorForType().findFieldByName("message")));
    }

    @Test
    public void testComplexRpcInputAndOutputRoundTripYgot() {
        YangDataProtoCodec<?, ?> inputCodec = YangDataProtoCodec.getInstance(updateConfigInput, ProtoCodecMode.YGOT);
        YangDataProtoCodec<?, ?> outputCodec = YangDataProtoCodec.getInstance(updateConfigOutput, ProtoCodecMode.YGOT);

        DynamicMessage inputMessage = buildYgotUpdateConfigInputMessage();
        ValidatorResultBuilder inputValidator = new ValidatorResultBuilder();
        InputData inputData = (InputData) inputCodec.deserialize(inputMessage, inputValidator);
        assertNotNull(inputData, "YGOT complex RPC input should deserialize");
        assertTrue(inputValidator.build().isOk(), "YGOT complex RPC input should not add validation errors");

        ContainerData config = getRequiredContainer(inputData, "config");
        assertLeafString(config, "name", "feature-b");
        assertLeafString(config, "value", "disabled");
        assertLeafString(config, "enabled", "false");

        DynamicMessage serializedInput = inputCodec.serialize(inputData);
        DynamicMessage configMessage = (DynamicMessage) serializedInput.getField(
                serializedInput.getDescriptorForType().findFieldByName("config"));
        assertEquals("feature-b", unwrapWrapperValue((DynamicMessage) configMessage.getField(
                configMessage.getDescriptorForType().findFieldByName("name"))));
        assertEquals("disabled", unwrapWrapperValue((DynamicMessage) configMessage.getField(
                configMessage.getDescriptorForType().findFieldByName("value"))));
        assertEquals(false, unwrapWrapperValue((DynamicMessage) configMessage.getField(
                configMessage.getDescriptorForType().findFieldByName("enabled"))));

        DynamicMessage outputMessage = buildYgotUpdateConfigOutputMessage();
        ValidatorResultBuilder outputValidator = new ValidatorResultBuilder();
        OutPutData outputData = (OutPutData) outputCodec.deserialize(outputMessage, outputValidator);
        assertNotNull(outputData, "YGOT complex RPC output should deserialize");
        assertTrue(outputValidator.build().isOk(), "YGOT complex RPC output should not add validation errors");
        assertLeafString(outputData, "success", "false");
        assertLeafString(outputData, "message", "rolled-back");

        DynamicMessage serializedOutput = outputCodec.serialize(outputData);
        assertEquals(false, unwrapWrapperValue((DynamicMessage) serializedOutput.getField(
                serializedOutput.getDescriptorForType().findFieldByName("success"))));
        assertEquals("rolled-back", unwrapWrapperValue((DynamicMessage) serializedOutput.getField(
                serializedOutput.getDescriptorForType().findFieldByName("message"))));
    }

    @Test
    public void testRpcCodecDirectlyUsesRpcDescriptorContract() {
        YangDataProtoCodec<?, ?> rpcCodec = YangDataProtoCodec.getInstance(getInfoRpc, ProtoCodecMode.SIMPLE);
        assertTrue(rpcCodec instanceof RpcDataProtoCodec, "Direct RPC codec should be RpcDataProtoCodec");

        Descriptors.Descriptor rpcDescriptor = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoRpc);
        assertNotNull(rpcDescriptor, "Direct RPC descriptor should exist");
        assertNotNull(rpcDescriptor.findNestedTypeByName("GetInfoInput"));
        assertNotNull(rpcDescriptor.findNestedTypeByName("GetInfoOutput"));

        RpcData rpcData = (RpcData) YangDataBuilderFactory.getBuilder().getYangData(getInfoRpc, null);
        DynamicMessage serialized = rpcCodec.serialize(rpcData);
        assertNotNull(serialized, "Direct RPC codec should serialize an empty RPC wrapper message");
        assertEquals(0, serialized.getAllFields().size(),
                "Direct RPC wrapper message is currently expected to be empty while input/output stay nested types");
    }

    private static DynamicMessage buildSimpleGetInfoInputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoInput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("request_id"), "req-1")
                .setField(desc.findFieldByName("verbose"), true)
                .build();
    }

    private static DynamicMessage buildYgotGetInfoInputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(getInfoInput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("request_id"), wrapValue(desc.findFieldByName("request_id").getMessageType(), "req-2"))
                .setField(desc.findFieldByName("verbose"), wrapValue(desc.findFieldByName("verbose").getMessageType(), false))
                .build();
    }

    private static DynamicMessage buildSimpleGetInfoOutputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(getInfoOutput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("response_id"), "resp-1")
                .setField(desc.findFieldByName("status"), "ok")
                .setField(desc.findFieldByName("code"), 200)
                .build();
    }

    private static DynamicMessage buildYgotGetInfoOutputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(getInfoOutput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("response_id"), wrapValue(desc.findFieldByName("response_id").getMessageType(), "resp-2"))
                .setField(desc.findFieldByName("status"), wrapValue(desc.findFieldByName("status").getMessageType(), "warning"))
                .setField(desc.findFieldByName("code"), wrapValue(desc.findFieldByName("code").getMessageType(), 201L))
                .build();
    }

    private static DynamicMessage buildSimpleUpdateConfigInputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(updateConfigInput);
        Descriptors.FieldDescriptor configField = desc.findFieldByName("config");
        Descriptors.Descriptor configDesc = configField.getMessageType();
        DynamicMessage config = DynamicMessage.newBuilder(configDesc)
                .setField(configDesc.findFieldByName("name"), "feature-a")
                .setField(configDesc.findFieldByName("value"), "enabled")
                .setField(configDesc.findFieldByName("enabled"), true)
                .build();
        return DynamicMessage.newBuilder(desc)
                .setField(configField, config)
                .build();
    }

    private static DynamicMessage buildYgotUpdateConfigInputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(updateConfigInput);
        Descriptors.FieldDescriptor configField = desc.findFieldByName("config");
        Descriptors.Descriptor configDesc = configField.getMessageType();
        DynamicMessage config = DynamicMessage.newBuilder(configDesc)
                .setField(configDesc.findFieldByName("name"), wrapValue(configDesc.findFieldByName("name").getMessageType(), "feature-b"))
                .setField(configDesc.findFieldByName("value"), wrapValue(configDesc.findFieldByName("value").getMessageType(), "disabled"))
                .setField(configDesc.findFieldByName("enabled"), wrapValue(configDesc.findFieldByName("enabled").getMessageType(), false))
                .build();
        return DynamicMessage.newBuilder(desc)
                .setField(configField, config)
                .build();
    }

    private static DynamicMessage buildSimpleUpdateConfigOutputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE)
                .getDescriptor(updateConfigOutput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("success"), true)
                .setField(desc.findFieldByName("message"), "updated")
                .build();
    }

    private static DynamicMessage buildYgotUpdateConfigOutputMessage() {
        Descriptors.Descriptor desc = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT)
                .getDescriptor(updateConfigOutput);
        return DynamicMessage.newBuilder(desc)
                .setField(desc.findFieldByName("success"), wrapValue(desc.findFieldByName("success").getMessageType(), false))
                .setField(desc.findFieldByName("message"), wrapValue(desc.findFieldByName("message").getMessageType(), "rolled-back"))
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

    private static ContainerData getRequiredContainer(YangDataContainer container, String localName) {
        YangData<?> child = container.getDataChildren().stream()
                .filter(data -> data.getSchemaNode() != null && localName.equals(data.getSchemaNode().getArgStr()))
                .findFirst()
                .orElse(null);
        assertNotNull(child, "Expected container child: " + localName);
        assertTrue(child instanceof ContainerData, "Expected container data for child: " + localName);
        return (ContainerData) child;
    }

    private static void assertLeafString(YangDataContainer container, String localName, String expectedValue) {
        YangData<?> child = container.getDataChildren().stream()
                .filter(data -> data.getSchemaNode() != null && localName.equals(data.getSchemaNode().getArgStr()))
                .findFirst()
                .orElse(null);
        assertNotNull(child, "Expected leaf child: " + localName);
        assertTrue(child instanceof LeafData, "Expected leaf data for child: " + localName);
        assertEquals(expectedValue, ((LeafData<?>) child).getStringValue());
    }
}
