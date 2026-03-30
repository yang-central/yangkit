package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages protobuf {@link Descriptors.Descriptor} objects for YANG schema nodes.
 *
 * <p>Two singleton instances are maintained — one per {@link ProtoCodecMode}.
 * Descriptors are cached using the schema node's qualified name as key.
 *
 * <p>Schema generation is fully delegated to {@link ProtoSchemaGenerator};
 * this class is responsible only for:
 * <ol>
 *   <li>Finding the root YANG module for a schema node</li>
 *   <li>Building the protobuf {@link Descriptors.FileDescriptor} with the
 *       correct dependency set (ywrapper in YGOT mode)</li>
 *   <li>Locating the right message within the file descriptor</li>
 *   <li>Caching the result</li>
 * </ol>
 */
public class ProtoDescriptorManager {

    private static volatile ProtoDescriptorManager instanceSimple;
    private static volatile ProtoDescriptorManager instanceYgot;

    private final ProtoCodecMode mode;
    private final ConcurrentHashMap<String, Descriptors.Descriptor> cache =
            new ConcurrentHashMap<>();

    private ProtoDescriptorManager(ProtoCodecMode mode) {
        this.mode = mode;
    }

    // =========================================================================
    // Singleton accessors
    // =========================================================================

    /** Returns the manager for SIMPLE mode. */
    public static ProtoDescriptorManager getInstance() {
        return getInstance(ProtoCodecMode.SIMPLE);
    }

    /** Returns the manager for the given mode. */
    public static ProtoDescriptorManager getInstance(ProtoCodecMode mode) {
        if (mode == ProtoCodecMode.YGOT) {
            if (instanceYgot == null) {
                synchronized (ProtoDescriptorManager.class) {
                    if (instanceYgot == null) instanceYgot = new ProtoDescriptorManager(ProtoCodecMode.YGOT);
                }
            }
            return instanceYgot;
        } else {
            if (instanceSimple == null) {
                synchronized (ProtoDescriptorManager.class) {
                    if (instanceSimple == null) instanceSimple = new ProtoDescriptorManager(ProtoCodecMode.SIMPLE);
                }
            }
            return instanceSimple;
        }
    }

    // =========================================================================
    // Main API
    // =========================================================================

    /**
     * Returns the protobuf {@link Descriptors.Descriptor} for the given schema
     * node, creating and caching it if necessary.
     *
     * @param schemaNode the YANG schema node
     * @return the message descriptor, or {@code null} on error
     */
    public Descriptors.Descriptor getDescriptor(SchemaNode schemaNode) {
        if (schemaNode == null) return null;

        String key = cacheKey(schemaNode);
        Descriptors.Descriptor cached = cache.get(key);
        if (cached != null) return cached;

        Descriptors.Descriptor descriptor = createDescriptor(schemaNode);
        if (descriptor != null) {
            cache.put(key, descriptor);
        }
        return descriptor;
    }

    /** Clears all cached descriptors (useful for testing). */
    public void clearCache() {
        cache.clear();
    }

    // =========================================================================
    // Descriptor creation
    // =========================================================================

    private Descriptors.Descriptor createDescriptor(SchemaNode schemaNode) {
        try {
            // 1. Find the YANG module that owns this schema node
            Module module = findModule(schemaNode);
            if (module == null) {
                System.err.println("[ProtoDescriptorManager] Could not find module for: "
                        + schemaNode.getIdentifier());
                return null;
            }

            // 2. Generate the FileDescriptorProto
            ProtoSchemaGenerator gen = new ProtoSchemaGenerator(mode);
            DescriptorProtos.FileDescriptorProto fileProto = gen.generateFileDescriptor(module);
            if (fileProto == null) return null;

            // 3. Build with dependency set
            Descriptors.FileDescriptor[] deps = buildDependencies();
            Descriptors.FileDescriptor fileDesc =
                    Descriptors.FileDescriptor.buildFrom(fileProto, deps);

            // 4. Locate the message for this schema node
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

    /** Builds the dependency array for FileDescriptor.buildFrom(). */
    private Descriptors.FileDescriptor[] buildDependencies() {
        if (mode == ProtoCodecMode.YGOT) {
            return new Descriptors.FileDescriptor[]{
                    WrapperTypeManager.getInstance().getFileDescriptor()
            };
        }
        return new Descriptors.FileDescriptor[0];
    }

    /**
     * Searches the file descriptor for a message matching the schema node.
     * Handles both top-level messages and nested messages (for RPC input/output).
     */
    private Descriptors.Descriptor findMessageInFile(
            Descriptors.FileDescriptor fileDesc, SchemaNode schemaNode) {

        String name = ProtoSchemaGenerator.messageName((DataNode) schemaNode);

        // Try top-level first
        Descriptors.Descriptor topLevel = fileDesc.findMessageTypeByName(name);
        if (topLevel != null) return topLevel;

        // Search nested types (e.g. RPC input / output are nested inside the Rpc message)
        for (Descriptors.Descriptor msg : fileDesc.getMessageTypes()) {
            Descriptors.Descriptor nested = msg.findNestedTypeByName(name);
            if (nested != null) return nested;
        }

        return null;
    }

    // =========================================================================
    // Module resolution
    // =========================================================================

    /**
     * Finds the root YANG module for a schema node by using the context's
     * current module reference — the most reliable approach.
     */
    private static Module findModule(SchemaNode schemaNode) {
        // Primary: use the context's current module
        try {
            Module m = schemaNode.getContext().getCurModule();
            if (m != null) return m.getMainModule();
        } catch (Exception ignored) {}

        // Fallback: search schema context by module name from namespace
        try {
            String prefix = schemaNode.getIdentifier().getPrefix();
            if (prefix != null) {
                java.util.Optional<Module> opt =
                        schemaNode.getContext().getSchemaContext().getLatestModule(prefix);
                if (opt.isPresent()) return opt.get();
            }
        } catch (Exception ignored) {}

        // Last resort: walk the parent chain
        try {
            SchemaNodeContainer parent = schemaNode.getParentSchemaNode();
            while (parent != null) {
                if (parent instanceof Module) return (Module) parent;
                if (parent instanceof SchemaNode) {
                    parent = ((SchemaNode) parent).getParentSchemaNode();
                } else {
                    break;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    // =========================================================================
    // Cache key
    // =========================================================================

    private String cacheKey(SchemaNode schemaNode) {
        return mode.name() + ":" + schemaNode.getIdentifier().toString();
    }
}
