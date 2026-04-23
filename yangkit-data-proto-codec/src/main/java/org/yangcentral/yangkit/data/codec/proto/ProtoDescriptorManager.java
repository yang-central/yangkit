package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.data.codec.proto.convention.YangProtoConvention;
import org.yangcentral.yangkit.data.codec.proto.convention.YangProtoConventionRegistry;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages protobuf {@link Descriptors.Descriptor} objects for YANG schema nodes.
 *
 * <p>Instances are keyed by convention name. Use {@link #getInstance()} for the
 * default convention, or {@link #getInstance(String)} / {@link #getInstance(ProtoCodecMode)}
 * for a specific one.
 */
public class ProtoDescriptorManager {

    // Convention-keyed cache of manager instances
    private static final ConcurrentHashMap<String, ProtoDescriptorManager> INSTANCES =
            new ConcurrentHashMap<>();

    // Legacy per-mode fields kept for backward compatibility
    private static volatile ProtoDescriptorManager instanceSimple;
    private static volatile ProtoDescriptorManager instanceYgot;

    private final YangProtoConvention convention;
    /** @deprecated Use {@link #convention}. Kept for legacy bridge methods. */
    @Deprecated
    private final ProtoCodecMode mode;
    private final ConcurrentHashMap<String, Descriptors.Descriptor> cache =
            new ConcurrentHashMap<>();

    private ProtoDescriptorManager(YangProtoConvention convention) {
        this.convention = convention;
        this.mode = "ygot".equals(convention.getName()) ? ProtoCodecMode.YGOT : ProtoCodecMode.SIMPLE;
    }

    // ── Singleton accessors ───────────────────────────────────────────────────

    /** Returns the manager for the registry default convention. */
    public static ProtoDescriptorManager getInstance() {
        return getInstance(YangProtoConventionRegistry.getDefault().getName());
    }

    /** Returns the manager for the given convention name. */
    public static ProtoDescriptorManager getInstance(String conventionName) {
        return INSTANCES.computeIfAbsent(conventionName, name -> {
            YangProtoConvention c = YangProtoConventionRegistry.get(name);
            if (c == null) throw new IllegalArgumentException(
                    "Convention '" + name + "' is not registered.");
            return new ProtoDescriptorManager(c);
        });
    }

    /** @deprecated Use {@link #getInstance(String)}. */
    @Deprecated
    public static ProtoDescriptorManager getInstance(ProtoCodecMode mode) {
        return getInstance(mode.name().toLowerCase());
    }

    // ── Main API ──────────────────────────────────────────────────────────────

    /** Returns the convention this manager uses. */
    public YangProtoConvention getConvention() { return convention; }

    /**
     * Returns the protobuf {@link Descriptors.Descriptor} for the given schema node,
     * creating and caching it if necessary.
     */
    public Descriptors.Descriptor getDescriptor(SchemaNode schemaNode) {
        if (schemaNode == null) return null;
        String key = cacheKey(schemaNode);
        Descriptors.Descriptor cached = cache.get(key);
        if (cached != null) return cached;
        Descriptors.Descriptor descriptor = createDescriptor(schemaNode);
        if (descriptor != null) cache.put(key, descriptor);
        return descriptor;
    }

    /** Clears all cached descriptors (useful for testing). */
    public void clearCache() { cache.clear(); }

    // ── Descriptor creation ───────────────────────────────────────────────────

    private Descriptors.Descriptor createDescriptor(SchemaNode schemaNode) {
        try {
            Module module = findModule(schemaNode);
            if (module == null) {
                System.err.println("[ProtoDescriptorManager] Could not find module for: "
                        + schemaNode.getIdentifier());
                return null;
            }
            ProtoSchemaGenerator gen = new ProtoSchemaGenerator(convention);
            DescriptorProtos.FileDescriptorProto fileProto = gen.generateFileDescriptor(module);
            if (fileProto == null) return null;

            Descriptors.FileDescriptor[] deps = convention.getDependencies();
            Descriptors.FileDescriptor fileDesc =
                    Descriptors.FileDescriptor.buildFrom(fileProto, deps);
            return findMessageInFile(fileDesc, schemaNode);
        } catch (Descriptors.DescriptorValidationException e) {
            System.err.println("[ProtoDescriptorManager] Descriptor validation error for "
                    + schemaNode.getIdentifier() + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[ProtoDescriptorManager] Failed to create descriptor for "
                    + schemaNode.getIdentifier() + ": " + e.getMessage());
            return null;
        }
    }

    private Descriptors.Descriptor findMessageInFile(
            Descriptors.FileDescriptor fileDesc, SchemaNode schemaNode) {
        String name = ProtoSchemaGenerator.messageName(schemaNode);
        Descriptors.Descriptor topLevel = fileDesc.findMessageTypeByName(name);
        if (topLevel != null) return topLevel;
        for (Descriptors.Descriptor msg : fileDesc.getMessageTypes()) {
            Descriptors.Descriptor nested = findNestedMessageRecursive(msg, name);
            if (nested != null) return nested;
        }
        return null;
    }

    private Descriptors.Descriptor findNestedMessageRecursive(
            Descriptors.Descriptor parent, String name) {
        if (parent == null || name == null) return null;
        Descriptors.Descriptor direct = parent.findNestedTypeByName(name);
        if (direct != null) return direct;
        for (Descriptors.Descriptor nested : parent.getNestedTypes()) {
            Descriptors.Descriptor found = findNestedMessageRecursive(nested, name);
            if (found != null) return found;
        }
        return null;
    }

    // ── Module resolution ─────────────────────────────────────────────────────

    private static Module findModule(SchemaNode schemaNode) {
        try {
            Module m = schemaNode.getContext().getCurModule();
            if (m != null) return m.getMainModule();
        } catch (Exception ignored) {}
        try {
            String prefix = schemaNode.getIdentifier().getPrefix();
            if (prefix != null) {
                java.util.Optional<Module> opt =
                        schemaNode.getContext().getSchemaContext().getLatestModule(prefix);
                if (opt.isPresent()) return opt.get();
            }
        } catch (Exception ignored) {}
        try {
            SchemaNodeContainer parent = schemaNode.getParentSchemaNode();
            while (parent != null) {
                if (parent instanceof Module) return (Module) parent;
                if (parent instanceof SchemaNode) {
                    parent = ((SchemaNode) parent).getParentSchemaNode();
                } else break;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── Cache key ─────────────────────────────────────────────────────────────

    private String cacheKey(SchemaNode schemaNode) {
        return convention.getName() + ":" + schemaPathKey(schemaNode);
    }

    private static String schemaPathKey(SchemaNode schemaNode) {
        if (schemaNode == null) return "null";
        StringBuilder builder = new StringBuilder();
        SchemaNode current = schemaNode;
        while (current != null) {
            if (builder.length() > 0) builder.insert(0, '/');
            builder.insert(0, current.getClass().getSimpleName()
                    + "(" + current.getIdentifier().toString() + ")");
            SchemaNodeContainer parent = current.getParentSchemaNode();
            if (parent instanceof SchemaNode) current = (SchemaNode) parent;
            else current = null;
        }
        return builder.toString();
    }
}
