package org.yangcentral.yangkit.data.codec.proto.convention;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for {@link YangProtoConvention} instances.
 *
 * <p>Three conventions are pre-registered at startup:
 * <ul>
 *   <li>{@code "simple"} — primitive proto types, sequential field numbers (default)</li>
 *   <li>{@code "ygot"} — YGOT-compatible</li>
 *   <li>{@code "envelope"} — JSON envelope (inner format = json)</li>
 *   <li>{@code "envelope-xml"} — XML envelope</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * // Use the default convention (simple)
 * YangProtoConvention c = YangProtoConventionRegistry.getDefault();
 *
 * // Switch default
 * YangProtoConventionRegistry.setDefault("simple");
 *
 * // Register a custom convention
 * YangProtoConventionRegistry.register(new MyConvention());
 * }</pre>
 */
public class YangProtoConventionRegistry {

    private static final ConcurrentHashMap<String, YangProtoConvention> REGISTRY =
            new ConcurrentHashMap<>();

    private static volatile String defaultName = "simple";

    static {
        register(new YgotProtoConvention());
        register(new SimpleProtoConvention());
        register(new EnvelopeProtoConvention("envelope", "json",
                com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, "data"));
        register(new EnvelopeProtoConvention("envelope-xml", "xml",
                com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, "data"));
        register(new EnvelopeProtoConvention("envelope-json-string", "json",
                com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, "data"));
    }

    private YangProtoConventionRegistry() {}

    /**
     * Registers a convention. Replaces any existing registration with the same name.
     *
     * @param convention the convention to register; must not be {@code null}
     */
    public static void register(YangProtoConvention convention) {
        if (convention == null) throw new IllegalArgumentException("convention must not be null");
        REGISTRY.put(convention.getName(), convention);
    }

    /**
     * Returns the convention registered under {@code name}.
     *
     * @param name convention name
     * @return the convention, or {@code null} if not found
     */
    public static YangProtoConvention get(String name) {
        return REGISTRY.get(name);
    }

    /**
     * Returns the default convention (initially {@code "simple"}).
     *
     * @return default convention; never {@code null}
     * @throws IllegalStateException if the default convention has not been registered
     */
    public static YangProtoConvention getDefault() {
        YangProtoConvention c = REGISTRY.get(defaultName);
        if (c == null) throw new IllegalStateException(
                "Default convention '" + defaultName + "' is not registered.");
        return c;
    }

    /**
     * Sets the default convention by name.
     *
     * @param name the convention name to use as default
     * @throws IllegalArgumentException if the name has not been registered
     */
    public static void setDefault(String name) {
        if (!REGISTRY.containsKey(name))
            throw new IllegalArgumentException(
                    "Convention '" + name + "' is not registered. Register it first.");
        defaultName = name;
    }

    /** Returns the current default convention name. */
    public static String getDefaultName() {
        return defaultName;
    }
}

