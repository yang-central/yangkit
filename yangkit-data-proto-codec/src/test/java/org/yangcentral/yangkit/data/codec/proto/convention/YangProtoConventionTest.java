package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link YangProtoConventionRegistry} and {@link YangProtoConventionLoader}.
 */
class YangProtoConventionTest {

    @BeforeEach
    void resetDefault() {
        // Ensure the default is reset to "simple" before each test
        YangProtoConventionRegistry.setDefault("simple");
    }

    // ── Registry tests ────────────────────────────────────────────────────────

    @Test
    void builtinConventionsArePreRegistered() {
        assertNotNull(YangProtoConventionRegistry.get("ygot"),    "ygot should be pre-registered");
        assertNotNull(YangProtoConventionRegistry.get("simple"),  "simple should be pre-registered");
        assertNotNull(YangProtoConventionRegistry.get("envelope"), "envelope should be pre-registered");
        assertNotNull(YangProtoConventionRegistry.get("envelope-xml"), "envelope-xml should be pre-registered");
    }

    @Test
    void defaultIsSimple() {
        assertEquals("simple", YangProtoConventionRegistry.getDefaultName());
        assertEquals("simple", YangProtoConventionRegistry.getDefault().getName());
    }

    @Test
    void setDefaultChangesDefault() {
        YangProtoConventionRegistry.setDefault("ygot");
        assertEquals("ygot", YangProtoConventionRegistry.getDefaultName());
        YangProtoConventionRegistry.setDefault("simple"); // restore
    }

    @Test
    void setDefaultWithUnknownNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> YangProtoConventionRegistry.setDefault("nonexistent"));
    }

    @Test
    void registerCustomConvention() {
        YangProtoConvention custom = new SimpleProtoConvention() {
            @Override public String getName() { return "my-custom"; }
        };
        YangProtoConventionRegistry.register(custom);
        assertNotNull(YangProtoConventionRegistry.get("my-custom"));
        assertEquals("my-custom", YangProtoConventionRegistry.get("my-custom").getName());
    }

    // ── YgotProtoConvention tests ────────────────────────────────────────────

    @Test
    void ygotConventionHasBitsAndOneof() {
        YangProtoConvention ygot = YangProtoConventionRegistry.get("ygot");
        assertTrue(ygot.encodeBitsAsEnum(),   "YGOT: bits should be enum");
        assertTrue(ygot.encodeUnionAsOneof(), "YGOT: union should be oneof");
        assertFalse(ygot.isEnvelope());
    }

    @Test
    void ygotFieldNumberingIsFnv1a() {
        YangProtoConvention ygot = new YgotProtoConvention();
        java.util.Set<Integer> used = new java.util.HashSet<>();
        int n1 = ygot.nextFieldNumber("/module/container/leaf1", used);
        int n2 = ygot.nextFieldNumber("/module/container/leaf2", used);
        assertNotEquals(n1, n2);
        assertTrue(n1 >= 1 && n1 <= 536870911);
        // FNV-1a should be deterministic
        java.util.Set<Integer> used2 = new java.util.HashSet<>();
        int n1b = ygot.nextFieldNumber("/module/container/leaf1", used2);
        assertEquals(n1, n1b, "FNV-1a should be deterministic");
    }

    // ── SimpleProtoConvention tests ──────────────────────────────────────────

    @Test
    void simpleConventionNoBitsOrOneof() {
        YangProtoConvention simple = YangProtoConventionRegistry.get("simple");
        assertFalse(simple.encodeBitsAsEnum(),   "SIMPLE: bits should not be enum");
        assertFalse(simple.encodeUnionAsOneof(), "SIMPLE: union should not be oneof");
        assertNull(simple.getWrapperTypeName(null));
        assertFalse(simple.isEnvelope());
    }

    @Test
    void simpleFieldNumberingIsSequential() {
        YangProtoConvention simple = new SimpleProtoConvention();
        java.util.Set<Integer> used = new java.util.HashSet<>();
        assertEquals(1, simple.nextFieldNumber("/a", used));
        assertEquals(2, simple.nextFieldNumber("/b", used));
        assertEquals(3, simple.nextFieldNumber("/c", used));
    }

    // ── EnvelopeProtoConvention tests ────────────────────────────────────────

    @Test
    void envelopeConventionIsEnvelope() {
        YangProtoConvention env = YangProtoConventionRegistry.get("envelope");
        assertTrue(env.isEnvelope());
        assertEquals("json", env.getEnvelopeInnerFormat());
        assertEquals("data", env.getEnvelopeFieldName());
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES,
                env.getEnvelopeFieldType());
    }

    @Test
    void envelopeXmlConvention() {
        YangProtoConvention env = YangProtoConventionRegistry.get("envelope-xml");
        assertTrue(env.isEnvelope());
        assertEquals("xml", env.getEnvelopeInnerFormat());
    }

    // ── YAML loader tests ─────────────────────────────────────────────────────

    @Test
    void loadYgotFromYaml() {
        String yaml =
                "convention:\n  name: ygot-from-yaml\n  encoding-mode: structured\n" +
                "field-numbering: fnv1a\nscalar-wrapping: ywrapper\nbits-as: enum\nunion-as: oneof\n";
        YangProtoConvention c = YangProtoConventionLoader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("ygot-from-yaml", c.getName());
        assertTrue(c.encodeBitsAsEnum());
        assertTrue(c.encodeUnionAsOneof());
    }

    @Test
    void loadSimpleFromYaml() {
        String yaml =
                "convention:\n  name: simple-from-yaml\n  encoding-mode: structured\n" +
                "field-numbering: sequential\nscalar-wrapping: none\nbits-as: string\nunion-as: string\n";
        YangProtoConvention c = YangProtoConventionLoader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("simple-from-yaml", c.getName());
        assertFalse(c.encodeBitsAsEnum());
        assertFalse(c.encodeUnionAsOneof());
        java.util.Set<Integer> used = new java.util.HashSet<>();
        assertEquals(1, c.nextFieldNumber("/any", used));
    }

    @Test
    void loadEnvelopeFromYaml() {
        String yaml =
                "convention:\n  name: my-envelope\n  encoding-mode: envelope\n" +
                "envelope:\n  field-name: payload\n  field-type: bytes\n  inner-format: xml\n";
        YangProtoConvention c = YangProtoConventionLoader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("my-envelope", c.getName());
        assertTrue(c.isEnvelope());
        assertEquals("xml",     c.getEnvelopeInnerFormat());
        assertEquals("payload", c.getEnvelopeFieldName());
    }

    @Test
    void loadEnvelopeStringFieldTypeFromYaml() {
        String yaml =
                "convention:\n  name: env-str\n  encoding-mode: envelope\n" +
                "envelope:\n  field-name: data\n  field-type: string\n  inner-format: json\n";
        YangProtoConvention c = YangProtoConventionLoader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                c.getEnvelopeFieldType());
    }

    @Test
    void loadBuiltinClasspathYaml_ygot() {
        YangProtoConvention c = YangProtoConventionLoader.loadFromClasspath(
                "/convention/ygot-convention.yaml");
        assertEquals("ygot", c.getName());
        assertTrue(c.encodeBitsAsEnum());
    }

    @Test
    void loadBuiltinClasspathYaml_simple() {
        YangProtoConvention c = YangProtoConventionLoader.loadFromClasspath(
                "/convention/simple-convention.yaml");
        assertEquals("simple", c.getName());
        assertFalse(c.encodeBitsAsEnum());
    }

    @Test
    void loadBuiltinClasspathYaml_envelope() {
        YangProtoConvention c = YangProtoConventionLoader.loadFromClasspath(
                "/convention/envelope-json-convention.yaml");
        assertEquals("envelope", c.getName());
        assertTrue(c.isEnvelope());
        assertEquals("json", c.getEnvelopeInnerFormat());
    }
}

