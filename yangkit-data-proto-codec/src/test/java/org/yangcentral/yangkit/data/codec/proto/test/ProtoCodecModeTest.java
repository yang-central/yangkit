package org.yangcentral.yangkit.data.codec.proto.test;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.codec.proto.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProtoCodecMode-aware behavior: SIMPLE vs YGOT type mapping,
 * WrapperTypeManager, and mode-based codec factory.
 *
 * <p>Covers the critical fixes introduced in the codec rewrite:
 * <ul>
 *   <li>Integer subtype detection (getBaseTypeName fix)</li>
 *   <li>Boolean conversion fix (was always returning true for "false"/"0")</li>
 *   <li>Decimal64 parts encoding/decoding</li>
 *   <li>WrapperTypeManager descriptor availability</li>
 *   <li>SIMPLE vs YGOT field-type differences</li>
 *   <li>Mode propagation through YangDataProtoCodec.getInstance()</li>
 *   <li>ProtoSchemaGenerator mode-aware field types</li>
 * </ul>
 */
public class ProtoCodecModeTest {

    private static YangSchemaContext schemaContext;
    private static Module protoTypesModule;

    @BeforeAll
    static void setUp() throws DocumentException, IOException, YangParserException {
        String yangDir = ProtoCodecModeTest.class.getClassLoader()
                .getResource("proto/yang")
                .getFile();

        schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();

        if (!result.isOk() && result.getRecords() != null) {
            result.getRecords().forEach(r ->
                    System.out.println("  YANG validation: " + r.getSeverity() + ": " + r));
        }
        assertTrue(result.isOk(), "YANG modules should parse correctly");

        for (Module m : schemaContext.getModules()) {
            if ("test-proto-types".equals(m.getArgStr())) {
                protoTypesModule = m;
                break;
            }
        }
        assertNotNull(protoTypesModule, "test-proto-types module should be present");
    }

    // =========================================================================
    // ProtoCodecMode enum
    // =========================================================================

    @Test
    void testProtoCodecModeValues() {
        assertNotNull(ProtoCodecMode.SIMPLE);
        assertNotNull(ProtoCodecMode.YGOT);
        assertEquals(2, ProtoCodecMode.values().length);
    }

    // =========================================================================
    // WrapperTypeManager
    // =========================================================================

    @Test
    void testWrapperTypeManagerSingleton() {
        WrapperTypeManager w1 = WrapperTypeManager.getInstance();
        WrapperTypeManager w2 = WrapperTypeManager.getInstance();
        assertSame(w1, w2, "WrapperTypeManager should be a singleton");
    }

    @Test
    void testWrapperFileDescriptorNotNull() {
        Descriptors.FileDescriptor fd = WrapperTypeManager.getInstance().getFileDescriptor();
        assertNotNull(fd, "ywrapper FileDescriptor should be built successfully");
        assertEquals("ywrapper/ywrapper.proto", fd.getName());
        assertEquals("ywrapper", fd.getPackage());
    }

    @Test
    void testAllWrapperDescriptorsAvailable() {
        WrapperTypeManager mgr = WrapperTypeManager.getInstance();

        assertNotNull(mgr.getDescriptor(WrapperTypeManager.STRING_VALUE),
                "StringValue descriptor should exist");
        assertNotNull(mgr.getDescriptor(WrapperTypeManager.BOOL_VALUE),
                "BoolValue descriptor should exist");
        assertNotNull(mgr.getDescriptor(WrapperTypeManager.BYTES_VALUE),
                "BytesValue descriptor should exist");
        assertNotNull(mgr.getDescriptor(WrapperTypeManager.INT_VALUE),
                "IntValue descriptor should exist");
        assertNotNull(mgr.getDescriptor(WrapperTypeManager.UINT_VALUE),
                "UintValue descriptor should exist");
        assertNotNull(mgr.getDescriptor(WrapperTypeManager.DECIMAL64_VALUE),
                "Decimal64Value descriptor should exist");
    }

    @Test
    void testWrapperDescriptorFieldTypes() {
        WrapperTypeManager mgr = WrapperTypeManager.getInstance();

        // IntValue { sint64 value = 1; }
        Descriptors.Descriptor intValue = mgr.getDescriptor(WrapperTypeManager.INT_VALUE);
        assertNotNull(intValue);
        Descriptors.FieldDescriptor intField = intValue.findFieldByName("value");
        assertNotNull(intField, "IntValue should have a 'value' field");
        assertEquals(Descriptors.FieldDescriptor.Type.SINT64, intField.getType());

        // UintValue { uint64 value = 1; }
        Descriptors.Descriptor uintValue = mgr.getDescriptor(WrapperTypeManager.UINT_VALUE);
        Descriptors.FieldDescriptor uintField = uintValue.findFieldByName("value");
        assertNotNull(uintField, "UintValue should have a 'value' field");
        assertEquals(Descriptors.FieldDescriptor.Type.UINT64, uintField.getType());

        // Decimal64Value { uint64 digits = 1; uint32 precision = 2; }
        Descriptors.Descriptor dec64 = mgr.getDescriptor(WrapperTypeManager.DECIMAL64_VALUE);
        assertNotNull(dec64);
        assertNotNull(dec64.findFieldByName("digits"), "Decimal64Value should have 'digits' field");
        assertNotNull(dec64.findFieldByName("precision"), "Decimal64Value should have 'precision' field");
        assertEquals(Descriptors.FieldDescriptor.Type.UINT64,
                dec64.findFieldByName("digits").getType());
        assertEquals(Descriptors.FieldDescriptor.Type.UINT32,
                dec64.findFieldByName("precision").getType());

        // BoolValue { bool value = 1; }
        Descriptors.Descriptor boolValue = mgr.getDescriptor(WrapperTypeManager.BOOL_VALUE);
        Descriptors.FieldDescriptor boolField = boolValue.findFieldByName("value");
        assertEquals(Descriptors.FieldDescriptor.Type.BOOL, boolField.getType());

        // StringValue { string value = 1; }
        Descriptors.Descriptor stringValue = mgr.getDescriptor(WrapperTypeManager.STRING_VALUE);
        Descriptors.FieldDescriptor stringField = stringValue.findFieldByName("value");
        assertEquals(Descriptors.FieldDescriptor.Type.STRING, stringField.getType());

        // BytesValue { bytes value = 1; }
        Descriptors.Descriptor bytesValue = mgr.getDescriptor(WrapperTypeManager.BYTES_VALUE);
        Descriptors.FieldDescriptor bytesField = bytesValue.findFieldByName("value");
        assertEquals(Descriptors.FieldDescriptor.Type.BYTES, bytesField.getType());
    }

    // =========================================================================
    // YangProtoTypeMapper — boolean conversion fix
    // =========================================================================

    @Test
    void testBooleanConversionFix() {
        // The old code had a bug: it returned true for "false" and "0".
        // The fix: only "true" and "1" are truthy.
        //
        // We test via convertToProtoValue with a null type (best-effort path
        // which just preserves Boolean values unchanged) and via the integration
        // path through a real Type.  Here we test the helper in isolation by
        // using a YANG leaf's type from the loaded schema.

        Container mixed = findContainer("mixed-container");
        assertNotNull(mixed, "mixed-container should exist");

        Leaf boolLeaf = findLeaf(mixed, "bool-value");
        assertNotNull(boolLeaf, "bool-value leaf should exist");

        // "true" → true
        Object trueResult = YangProtoTypeMapper.convertToProtoValue("true", boolLeaf.getType());
        assertEquals(Boolean.TRUE, trueResult, "\"true\" should convert to Boolean.TRUE");

        // "1" → true
        Object oneResult = YangProtoTypeMapper.convertToProtoValue("1", boolLeaf.getType());
        assertEquals(Boolean.TRUE, oneResult, "\"1\" should convert to Boolean.TRUE");

        // "false" → false (was broken before fix)
        Object falseResult = YangProtoTypeMapper.convertToProtoValue("false", boolLeaf.getType());
        assertEquals(Boolean.FALSE, falseResult, "\"false\" should convert to Boolean.FALSE");

        // "0" → false (was broken before fix)
        Object zeroResult = YangProtoTypeMapper.convertToProtoValue("0", boolLeaf.getType());
        assertEquals(Boolean.FALSE, zeroResult, "\"0\" should convert to Boolean.FALSE");
    }

    // =========================================================================
    // YangProtoTypeMapper — Decimal64 parts encoding
    // =========================================================================

    @Test
    void testDecimal64PartsRoundTrip() {
        long[] parts = YangProtoTypeMapper.toDecimal64Parts("3.1416", 4);
        assertEquals(31416L, parts[0], "digits should be 31416 for 3.1416 with fractionDigits=4");
        assertEquals(4L, parts[1], "precision should be 4");

        String back = YangProtoTypeMapper.fromDecimal64Parts(parts[0], (int) parts[1]);
        assertEquals("3.1416", back, "round-tripped decimal should equal original");
    }

    @Test
    void testDecimal64PartsZero() {
        long[] parts = YangProtoTypeMapper.toDecimal64Parts("0.0000", 4);
        assertEquals(0L, parts[0]);
        String back = YangProtoTypeMapper.fromDecimal64Parts(parts[0], (int) parts[1]);
        assertEquals("0.0000", back);
    }

    @Test
    void testDecimal64PartsNegative() {
        long[] parts = YangProtoTypeMapper.toDecimal64Parts("-1.5", 2);
        assertEquals(-150L, parts[0]);
        String back = YangProtoTypeMapper.fromDecimal64Parts(parts[0], (int) parts[1]);
        assertEquals("-1.50", back);
    }

    // =========================================================================
    // YangProtoTypeMapper — getBaseTypeName + SIMPLE field types
    // =========================================================================

    @Test
    void testIntegerLeafTypesInSimpleMode() {
        // mixed-container has int-value (int32) and uint-value (uint64)
        Container mixed = findContainer("mixed-container");
        assertNotNull(mixed);

        Leaf intLeaf = findLeaf(mixed, "int-value");
        assertNotNull(intLeaf, "int-value leaf should exist");
        DescriptorProtos.FieldDescriptorProto.Type intType =
                YangProtoTypeMapper.getProtoFieldType(intLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32, intType,
                "int32 should map to TYPE_INT32 in SIMPLE mode");

        Leaf uintLeaf = findLeaf(mixed, "uint-value");
        assertNotNull(uintLeaf, "uint-value leaf should exist");
        DescriptorProtos.FieldDescriptorProto.Type uintType =
                YangProtoTypeMapper.getProtoFieldType(uintLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64, uintType,
                "uint64 should map to TYPE_UINT64 in SIMPLE mode");
    }

    @Test
    void testStringLeafTypeInSimpleMode() {
        Container mixed = findContainer("mixed-container");
        Leaf strLeaf = findLeaf(mixed, "str-value");
        assertNotNull(strLeaf);

        DescriptorProtos.FieldDescriptorProto.Type t =
                YangProtoTypeMapper.getProtoFieldType(strLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, t);
    }

    @Test
    void testBooleanLeafTypeInSimpleMode() {
        Container mixed = findContainer("mixed-container");
        Leaf boolLeaf = findLeaf(mixed, "bool-value");
        assertNotNull(boolLeaf);

        DescriptorProtos.FieldDescriptorProto.Type t =
                YangProtoTypeMapper.getProtoFieldType(boolLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL, t);
    }

    @Test
    void testBinaryLeafTypeInSimpleMode() {
        Container mixed = findContainer("mixed-container");
        Leaf binLeaf = findLeaf(mixed, "binary-value");
        assertNotNull(binLeaf);

        DescriptorProtos.FieldDescriptorProto.Type t =
                YangProtoTypeMapper.getProtoFieldType(binLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, t);
    }

    @Test
    void testEnumerationLeafTypeInBothModes() {
        Container enumContainer = findContainer("enum-container");
        assertNotNull(enumContainer, "enum-container should exist");

        Leaf statusLeaf = findLeaf(enumContainer, "status");
        assertNotNull(statusLeaf, "status leaf should exist");

        // Both SIMPLE and YGOT should map enumeration to TYPE_ENUM
        DescriptorProtos.FieldDescriptorProto.Type simpleType =
                YangProtoTypeMapper.getProtoFieldType(statusLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, simpleType,
                "enumeration should be TYPE_ENUM in SIMPLE mode");

        DescriptorProtos.FieldDescriptorProto.Type ygotType =
                YangProtoTypeMapper.getProtoFieldType(statusLeaf.getType(), ProtoCodecMode.YGOT);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, ygotType,
                "enumeration should be TYPE_ENUM in YGOT mode");
    }

    // =========================================================================
    // YangProtoTypeMapper — YGOT wrapper type names
    // =========================================================================

    @Test
    void testYwrapperTypeNamesForScalars() {
        Container mixed = findContainer("mixed-container");

        Leaf intLeaf = findLeaf(mixed, "int-value");
        assertEquals(".ywrapper.IntValue",
                YangProtoTypeMapper.getYwrapperTypeName(intLeaf.getType()),
                "int32 should map to .ywrapper.IntValue");

        Leaf uintLeaf = findLeaf(mixed, "uint-value");
        assertEquals(".ywrapper.UintValue",
                YangProtoTypeMapper.getYwrapperTypeName(uintLeaf.getType()),
                "uint64 should map to .ywrapper.UintValue");

        Leaf strLeaf = findLeaf(mixed, "str-value");
        assertEquals(".ywrapper.StringValue",
                YangProtoTypeMapper.getYwrapperTypeName(strLeaf.getType()),
                "string should map to .ywrapper.StringValue");

        Leaf boolLeaf = findLeaf(mixed, "bool-value");
        assertEquals(".ywrapper.BoolValue",
                YangProtoTypeMapper.getYwrapperTypeName(boolLeaf.getType()),
                "boolean should map to .ywrapper.BoolValue");

        Leaf binLeaf = findLeaf(mixed, "binary-value");
        assertEquals(".ywrapper.BytesValue",
                YangProtoTypeMapper.getYwrapperTypeName(binLeaf.getType()),
                "binary should map to .ywrapper.BytesValue");

        Leaf decLeaf = findLeaf(mixed, "dec-value");
        assertEquals(".ywrapper.Decimal64Value",
                YangProtoTypeMapper.getYwrapperTypeName(decLeaf.getType()),
                "decimal64 should map to .ywrapper.Decimal64Value");
    }

    @Test
    void testYwrapperTypeNameForEnumIsNull() {
        // enum and bits use their own generated enum descriptors, not ywrapper
        Container enumContainer = findContainer("enum-container");
        Leaf statusLeaf = findLeaf(enumContainer, "status");
        assertNotNull(statusLeaf);

        String typeName = YangProtoTypeMapper.getYwrapperTypeName(statusLeaf.getType());
        assertNull(typeName, "enumeration type should return null from getYwrapperTypeName");
    }

    // =========================================================================
    // YGOT mode — scalars become TYPE_MESSAGE
    // =========================================================================

    @Test
    void testScalarsAreMessageTypeInYgotMode() {
        Container mixed = findContainer("mixed-container");

        Leaf intLeaf  = findLeaf(mixed, "int-value");
        Leaf strLeaf  = findLeaf(mixed, "str-value");
        Leaf boolLeaf = findLeaf(mixed, "bool-value");
        Leaf binLeaf  = findLeaf(mixed, "binary-value");
        Leaf decLeaf  = findLeaf(mixed, "dec-value");

        for (Leaf leaf : new Leaf[]{intLeaf, strLeaf, boolLeaf, binLeaf, decLeaf}) {
            assertNotNull(leaf);
            DescriptorProtos.FieldDescriptorProto.Type t =
                    YangProtoTypeMapper.getProtoFieldType(leaf.getType(), ProtoCodecMode.YGOT);
            assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE, t,
                    leaf.getArgStr() + " should be TYPE_MESSAGE in YGOT mode");
        }
    }

    @Test
    void testScalarsArePrimitiveInSimpleMode() {
        Container mixed = findContainer("mixed-container");

        Leaf intLeaf  = findLeaf(mixed, "int-value");
        Leaf strLeaf  = findLeaf(mixed, "str-value");
        Leaf boolLeaf = findLeaf(mixed, "bool-value");
        Leaf binLeaf  = findLeaf(mixed, "binary-value");

        assertNotEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                YangProtoTypeMapper.getProtoFieldType(intLeaf.getType(), ProtoCodecMode.SIMPLE),
                "int32 should NOT be TYPE_MESSAGE in SIMPLE mode");
        assertNotEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                YangProtoTypeMapper.getProtoFieldType(strLeaf.getType(), ProtoCodecMode.SIMPLE),
                "string should NOT be TYPE_MESSAGE in SIMPLE mode");
        assertNotEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                YangProtoTypeMapper.getProtoFieldType(boolLeaf.getType(), ProtoCodecMode.SIMPLE),
                "boolean should NOT be TYPE_MESSAGE in SIMPLE mode");
        assertNotEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                YangProtoTypeMapper.getProtoFieldType(binLeaf.getType(), ProtoCodecMode.SIMPLE),
                "binary should NOT be TYPE_MESSAGE in SIMPLE mode");
    }

    // =========================================================================
    // ProtoSchemaGenerator — mode-based file descriptor generation
    // =========================================================================

    @Test
    void testSchemaGeneratorSimpleModeDescriptor() {
        ProtoSchemaGenerator gen = new ProtoSchemaGenerator(ProtoCodecMode.SIMPLE);
        DescriptorProtos.FileDescriptorProto fdp = gen.generateFileDescriptor(protoTypesModule);
        assertNotNull(fdp, "SIMPLE mode should generate a FileDescriptorProto");
        assertTrue(fdp.getMessageTypeCount() > 0, "Should have at least one message type");
        // SIMPLE mode should NOT depend on ywrapper
        assertFalse(fdp.getDependencyList().contains("ywrapper/ywrapper.proto"),
                "SIMPLE mode should not import ywrapper.proto");
    }

    @Test
    void testSchemaGeneratorYgotModeDescriptor() {
        ProtoSchemaGenerator gen = new ProtoSchemaGenerator(ProtoCodecMode.YGOT);
        DescriptorProtos.FileDescriptorProto fdp = gen.generateFileDescriptor(protoTypesModule);
        assertNotNull(fdp, "YGOT mode should generate a FileDescriptorProto");
        assertTrue(fdp.getMessageTypeCount() > 0, "Should have at least one message type");
        // YGOT mode MUST depend on ywrapper
        assertTrue(fdp.getDependencyList().contains("ywrapper/ywrapper.proto"),
                "YGOT mode should import ywrapper.proto");
    }

    @Test
    void testSchemaGeneratorNullModuleReturnsNull() {
        ProtoSchemaGenerator gen = new ProtoSchemaGenerator(ProtoCodecMode.SIMPLE);
        assertNull(gen.generateFileDescriptor(null), "null module should return null");
    }

    @Test
    void testSchemaGeneratorGeneratesEnumContainer() {
        ProtoSchemaGenerator gen = new ProtoSchemaGenerator(ProtoCodecMode.SIMPLE);
        DescriptorProtos.FileDescriptorProto fdp = gen.generateFileDescriptor(protoTypesModule);
        assertNotNull(fdp);

        // enum-container message should be present
        boolean foundEnumContainer = false;
        for (DescriptorProtos.DescriptorProto msg : fdp.getMessageTypeList()) {
            if ("EnumContainer".equals(msg.getName()) || "enum_container".equals(msg.getName())) {
                foundEnumContainer = true;
                break;
            }
        }
        assertTrue(foundEnumContainer, "Should generate EnumContainer message");
    }

    // =========================================================================
    // YangDataProtoCodec.getInstance() — mode propagation
    // =========================================================================

    @Test
    void testGetInstanceDefaultIsSimple() {
        Container container = findContainer("mixed-container");
        assertNotNull(container);

        YangDataProtoCodec<?, ?> codec = YangDataProtoCodec.getInstance(container);
        assertNotNull(codec);
        assertEquals(ProtoCodecMode.SIMPLE, codec.getMode(),
                "Default getInstance() should return SIMPLE mode");
    }

    @Test
    void testGetInstanceExplicitSimpleMode() {
        Container container = findContainer("mixed-container");
        YangDataProtoCodec<?, ?> codec =
                YangDataProtoCodec.getInstance(container, ProtoCodecMode.SIMPLE);
        assertNotNull(codec);
        assertEquals(ProtoCodecMode.SIMPLE, codec.getMode());
        assertTrue(codec instanceof ContainerDataProtoCodec);
    }

    @Test
    void testGetInstanceExplicitYgotMode() {
        Container container = findContainer("mixed-container");
        YangDataProtoCodec<?, ?> codec =
                YangDataProtoCodec.getInstance(container, ProtoCodecMode.YGOT);
        assertNotNull(codec);
        assertEquals(ProtoCodecMode.YGOT, codec.getMode(),
                "YGOT getInstance() should return YGOT mode codec");
        assertTrue(codec instanceof ContainerDataProtoCodec);
    }

    @Test
    void testGetInstanceForLeaf() {
        Container mixed = findContainer("mixed-container");
        Leaf strLeaf = findLeaf(mixed, "str-value");
        assertNotNull(strLeaf);

        YangDataProtoCodec<?, ?> simpleCodec =
                YangDataProtoCodec.getInstance(strLeaf, ProtoCodecMode.SIMPLE);
        YangDataProtoCodec<?, ?> ygotCodec =
                YangDataProtoCodec.getInstance(strLeaf, ProtoCodecMode.YGOT);

        assertNotNull(simpleCodec);
        assertNotNull(ygotCodec);
        assertTrue(simpleCodec instanceof LeafDataProtoCodec);
        assertTrue(ygotCodec instanceof LeafDataProtoCodec);
        assertEquals(ProtoCodecMode.SIMPLE, simpleCodec.getMode());
        assertEquals(ProtoCodecMode.YGOT, ygotCodec.getMode());
    }

    @Test
    void testGetInstanceForNullReturnsNull() {
        assertNull(YangDataProtoCodec.getInstance(null));
        assertNull(YangDataProtoCodec.getInstance(null, ProtoCodecMode.YGOT));
    }

    @Test
    void testGetInstanceNullModeDefaultsToSimple() {
        Container container = findContainer("mixed-container");
        YangDataProtoCodec<?, ?> codec =
                YangDataProtoCodec.getInstance(container, null);
        assertNotNull(codec);
        assertEquals(ProtoCodecMode.SIMPLE, codec.getMode(),
                "null mode should default to SIMPLE");
    }

    // =========================================================================
    // ProtoDescriptorManager — mode-based singleton instances
    // =========================================================================

    @Test
    void testDescriptorManagerTwoSingletons() {
        ProtoDescriptorManager simple = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE);
        ProtoDescriptorManager ygot   = ProtoDescriptorManager.getInstance(ProtoCodecMode.YGOT);

        assertNotNull(simple, "SIMPLE descriptor manager should not be null");
        assertNotNull(ygot,   "YGOT descriptor manager should not be null");
        assertNotSame(simple, ygot, "SIMPLE and YGOT managers should be separate instances");
    }

    @Test
    void testDescriptorManagerDefaultIsSimple() {
        ProtoDescriptorManager defaultMgr = ProtoDescriptorManager.getInstance();
        ProtoDescriptorManager simpleMgr  = ProtoDescriptorManager.getInstance(ProtoCodecMode.SIMPLE);
        assertSame(defaultMgr, simpleMgr, "Default manager should be the SIMPLE singleton");
    }

    // =========================================================================
    // Bits container — schema presence verification
    // =========================================================================

    @Test
    void testBitsContainerPresent() {
        Container bitsContainer = findContainer("bits-container");
        assertNotNull(bitsContainer, "bits-container should be present in the YANG module");

        Leaf permsLeaf = findLeaf(bitsContainer, "permissions");
        assertNotNull(permsLeaf, "permissions bits leaf should exist");

        // In SIMPLE mode, bits map to TYPE_ENUM
        DescriptorProtos.FieldDescriptorProto.Type simpleType =
                YangProtoTypeMapper.getProtoFieldType(permsLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, simpleType,
                "bits should map to TYPE_ENUM in SIMPLE mode");

        // In YGOT mode, bits also map to TYPE_ENUM
        DescriptorProtos.FieldDescriptorProto.Type ygotType =
                YangProtoTypeMapper.getProtoFieldType(permsLeaf.getType(), ProtoCodecMode.YGOT);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, ygotType,
                "bits should map to TYPE_ENUM in YGOT mode");
    }

    // =========================================================================
    // Union container — schema presence verification
    // =========================================================================

    @Test
    void testUnionContainerPresent() {
        Container unionContainer = findContainer("union-container");
        assertNotNull(unionContainer, "union-container should be present");

        Leaf flexLeaf = findLeaf(unionContainer, "flexible-value");
        assertNotNull(flexLeaf, "flexible-value union leaf should exist");

        // SIMPLE mode: union → TYPE_STRING
        DescriptorProtos.FieldDescriptorProto.Type simpleType =
                YangProtoTypeMapper.getProtoFieldType(flexLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, simpleType,
                "union should map to TYPE_STRING in SIMPLE mode");

        // YGOT mode: union → TYPE_MESSAGE (oneof placeholder)
        DescriptorProtos.FieldDescriptorProto.Type ygotType =
                YangProtoTypeMapper.getProtoFieldType(flexLeaf.getType(), ProtoCodecMode.YGOT);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE, ygotType,
                "union should map to TYPE_MESSAGE in YGOT mode (oneof placeholder)");
    }

    // =========================================================================
    // Leafref container — schema presence verification
    // =========================================================================

    @Test
    void testLeafrefContainerPresent() {
        Container leafrefContainer = findContainer("leafref-container");
        assertNotNull(leafrefContainer, "leafref-container should be present");

        Leaf refLeaf = findLeaf(leafrefContainer, "ref-interface");
        assertNotNull(refLeaf, "ref-interface leafref leaf should exist");

        // SIMPLE mode: leafref → TYPE_STRING
        DescriptorProtos.FieldDescriptorProto.Type t =
                YangProtoTypeMapper.getProtoFieldType(refLeaf.getType(), ProtoCodecMode.SIMPLE);
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, t,
                "leafref should map to TYPE_STRING in SIMPLE mode");
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Container findContainer(String name) {
        if (protoTypesModule == null) return null;
        for (Object child : protoTypesModule.getDataNodeChildren()) {
            if (child instanceof Container) {
                Container c = (Container) child;
                if (name.equals(c.getArgStr())) return c;
            }
        }
        return null;
    }

    private Leaf findLeaf(Container container, String name) {
        if (container == null) return null;
        for (Object child : container.getDataNodeChildren()) {
            if (child instanceof Leaf) {
                Leaf l = (Leaf) child;
                if (name.equals(l.getArgStr())) return l;
            }
        }
        return null;
    }
}
