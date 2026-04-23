package org.yangcentral.yangkit.data.codec.proto.convention;

import com.google.protobuf.DescriptorProtos;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads a {@link YangProtoConvention} from a YAML descriptor file.
 *
 * <p>YAML format:
 * <pre>{@code
 * convention:
 *   name: "my-convention"
 *   description: "Optional description"
 *   encoding-mode: "structured"      # structured | envelope
 *
 * # Only used when encoding-mode=envelope:
 * envelope:
 *   field-name: "data"
 *   field-type: "bytes"              # bytes | string
 *   inner-format: "json"             # json | xml
 *
 * # Only used when encoding-mode=structured:
 * field-numbering: "sequential"      # sequential | fnv1a
 * scalar-wrapping: "none"            # none | ywrapper
 * bits-as: "string"                  # string | enum
 * union-as: "string"                 # string | oneof
 * }</pre>
 *
 * <p>Usage:
 * <pre>{@code
 * YangProtoConvention c = YangProtoConventionLoader.loadFromClasspath(
 *         "/convention/my-convention.yaml");
 * YangProtoConventionRegistry.register(c);
 * }</pre>
 */
public class YangProtoConventionLoader {

    private YangProtoConventionLoader() {}

    /**
     * Loads a convention from the classpath resource at {@code resourcePath}.
     *
     * @param resourcePath classpath resource path, e.g. {@code "/convention/simple-convention.yaml"}
     * @return loaded convention
     * @throws IllegalArgumentException if the resource is not found or the YAML is invalid
     */
    public static YangProtoConvention loadFromClasspath(String resourcePath) {
        InputStream in = YangProtoConventionLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException(
                    "Convention YAML resource not found: " + resourcePath);
        }
        return load(in);
    }

    /**
     * Loads a convention from a file system path.
     *
     * @param path path to the YAML file
     * @return loaded convention
     */
    public static YangProtoConvention loadFromFile(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return load(in);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException(
                    "Cannot read convention YAML file: " + path, e);
        }
    }

    /**
     * Loads a convention from an {@link InputStream}.
     *
     * @param in YAML input stream
     * @return loaded convention
     */
    public static YangProtoConvention load(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(in);
        if (root == null) {
            throw new IllegalArgumentException("Empty or invalid convention YAML");
        }
        return parseConvention(root);
    }

    @SuppressWarnings("unchecked")
    private static YangProtoConvention parseConvention(Map<String, Object> root) {
        // ── Metadata ──────────────────────────────────────────────────────────
        Map<String, Object> meta = (Map<String, Object>) root.get("convention");
        String name = meta != null ? str(meta, "name", "custom") : "custom";
        String encodingMode = meta != null ? str(meta, "encoding-mode", "structured") : "structured";

        // ── Envelope mode ─────────────────────────────────────────────────────
        if ("envelope".equals(encodingMode)) {
            Map<String, Object> env = (Map<String, Object>) root.get("envelope");
            String fieldName   = env != null ? str(env, "field-name",   "data") : "data";
            String fieldTypeStr = env != null ? str(env, "field-type",  "bytes") : "bytes";
            String innerFormat  = env != null ? str(env, "inner-format","json")  : "json";

            DescriptorProtos.FieldDescriptorProto.Type fieldType =
                    "string".equalsIgnoreCase(fieldTypeStr)
                            ? DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING
                            : DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;

            return new EnvelopeProtoConvention(name, innerFormat, fieldType, fieldName);
        }

        // ── Structured mode ───────────────────────────────────────────────────
        String fieldNumbering = str(root, "field-numbering", "sequential");
        String scalarWrapping = str(root, "scalar-wrapping", "none");
        String bitsAs         = str(root, "bits-as",         "string");
        String unionAs        = str(root, "union-as",        "string");

        boolean fnv1a      = "fnv1a".equalsIgnoreCase(fieldNumbering);
        boolean ywrapper   = "ywrapper".equalsIgnoreCase(scalarWrapping);
        boolean bitsEnum   = "enum".equalsIgnoreCase(bitsAs);
        boolean unionOneof = "oneof".equalsIgnoreCase(unionAs);

        if (fnv1a && ywrapper && bitsEnum && unionOneof) {
            // Matches YGOT exactly — delegate to the canonical implementation
            return new NamedDelegateConvention(name, new YgotProtoConvention());
        }
        if (!fnv1a && !ywrapper && !bitsEnum && !unionOneof) {
            // Matches SIMPLE exactly
            return new NamedDelegateConvention(name, new SimpleProtoConvention());
        }

        // Custom combination — return a configurable convention
        return new ConfigurableProtoConvention(name, fnv1a, ywrapper, bitsEnum, unionOneof);
    }

    private static String str(Map<String, Object> m, String key, String defaultValue) {
        Object v = m.get(key);
        return v != null ? v.toString() : defaultValue;
    }

    // ── Configurable convention (custom YAML-defined combinations) ─────────

    private static final class ConfigurableProtoConvention implements YangProtoConvention {
        private final String name;
        private final boolean fnv1a, ywrapper, bitsEnum, unionOneof;

        ConfigurableProtoConvention(String name, boolean fnv1a, boolean ywrapper,
                                    boolean bitsEnum, boolean unionOneof) {
            this.name = name; this.fnv1a = fnv1a; this.ywrapper = ywrapper;
            this.bitsEnum = bitsEnum; this.unionOneof = unionOneof;
        }

        @Override public String getName() { return name; }
        @Override public boolean encodeBitsAsEnum()   { return bitsEnum; }
        @Override public boolean encodeUnionAsOneof() { return unionOneof; }

        @Override
        public DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(
                org.yangcentral.yangkit.model.api.stmt.Type yangType) {
            if (ywrapper && getWrapperTypeName(yangType) != null) {
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
            }
            return org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper
                    .getProtoFieldType(yangType,
                            org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode.SIMPLE);
        }

        @Override
        public String getWrapperTypeName(org.yangcentral.yangkit.model.api.stmt.Type yangType) {
            return ywrapper
                    ? org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper
                            .getYwrapperTypeName(yangType)
                    : null;
        }

        @Override
        public int nextFieldNumber(String fieldPath, java.util.Set<Integer> usedNumbers) {
            if (fnv1a) return YgotProtoConvention.fnvFieldNumber(fieldPath, usedNumbers);
            int n = 1;
            while (usedNumbers.contains(n)) n++;
            usedNumbers.add(n);
            return n;
        }

        @Override
        public Object toProtoValue(Object yangValue,
                                   org.yangcentral.yangkit.model.api.stmt.Type yangType) {
            return org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper
                    .convertToProtoValue(yangValue != null ? yangValue.toString() : null, yangType);
        }

        @Override
        public Object toYangValue(Object protoValue,
                                  org.yangcentral.yangkit.model.api.stmt.Type yangType) {
            return org.yangcentral.yangkit.data.codec.proto.YangProtoTypeMapper
                    .convertToYangValue(protoValue, yangType);
        }
    }

    /** Wrapper that gives a different name to an existing convention. */
    private static final class NamedDelegateConvention implements YangProtoConvention {
        private final String name;
        private final YangProtoConvention delegate;

        NamedDelegateConvention(String name, YangProtoConvention delegate) {
            this.name = name; this.delegate = delegate;
        }

        @Override public String getName() { return name; }
        @Override public boolean encodeBitsAsEnum()   { return delegate.encodeBitsAsEnum(); }
        @Override public boolean encodeUnionAsOneof() { return delegate.encodeUnionAsOneof(); }
        @Override public DescriptorProtos.FieldDescriptorProto.Type getProtoFieldType(
                org.yangcentral.yangkit.model.api.stmt.Type t) { return delegate.getProtoFieldType(t); }
        @Override public String getWrapperTypeName(
                org.yangcentral.yangkit.model.api.stmt.Type t) { return delegate.getWrapperTypeName(t); }
        @Override public int nextFieldNumber(String p, java.util.Set<Integer> u) {
            return delegate.nextFieldNumber(p, u); }
        @Override public Object toProtoValue(Object v,
                org.yangcentral.yangkit.model.api.stmt.Type t) { return delegate.toProtoValue(v, t); }
        @Override public Object toYangValue(Object v,
                org.yangcentral.yangkit.model.api.stmt.Type t) { return delegate.toYangValue(v, t); }
        @Override public com.google.protobuf.Descriptors.FileDescriptor[] getDependencies() {
            return delegate.getDependencies(); }
        @Override public String[] getDependencyImports() { return delegate.getDependencyImports(); }
    }
}

