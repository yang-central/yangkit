package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.codec.proto.convention.YangProtoConvention;
import org.yangcentral.yangkit.data.codec.proto.convention.YangProtoConventionRegistry;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.List;

/**
 * Abstract base class for bidirectional YANG data ↔ Protobuf conversion.
 *
 * <p>The codec mode ({@link ProtoCodecMode#SIMPLE} or {@link ProtoCodecMode#YGOT})
 * controls type mapping, field numbering, and wrapper-message usage.  All
 * concrete subclasses receive the mode through their constructors and
 * propagate it to {@link ProtoDescriptorManager}.
 *
 * <h3>Obtaining a codec instance</h3>
 * <pre>{@code
 * // SIMPLE mode (default)
 * YangDataProtoCodec<?,?> c = YangDataProtoCodec.getInstance(schemaNode);
 *
 * // YGOT mode
 * YangDataProtoCodec<?,?> c = YangDataProtoCodec.getInstance(schemaNode, ProtoCodecMode.YGOT);
 * }</pre>
 *
 * @param <S> the YANG schema-node type
 * @param <T> the YANG data type
 */
public abstract class YangDataProtoCodec<S extends SchemaNode, T extends YangData<S>>
        implements YangDataCodec<S, T, DynamicMessage> {

    private final S schemaNode;
    protected final ProtoCodecMode mode;
    protected final YangProtoConvention convention;
    private AnydataValidationContextResolver anydataValidationContextResolver;
    private String sourcePath;

    protected YangDataProtoCodec(S schemaNode, ProtoCodecMode mode) {
        this.schemaNode = schemaNode;
        this.mode       = mode;
        YangProtoConvention c = YangProtoConventionRegistry.get(mode.name().toLowerCase());
        this.convention = c != null ? c : YangProtoConventionRegistry.getDefault();
    }

    protected YangDataProtoCodec(S schemaNode, YangProtoConvention convention) {
        this.schemaNode = schemaNode;
        this.convention = convention;
        this.mode = "ygot".equals(convention.getName()) ? ProtoCodecMode.YGOT : ProtoCodecMode.SIMPLE;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaNode.getContext().getSchemaContext();
    }

    @Override
    public S getSchemaNode() {
        return schemaNode;
    }

    /** Returns the codec mode used by this instance. */
    public ProtoCodecMode getMode() {
        return mode;
    }

    // =========================================================================
    // Factory
    // =========================================================================

    /** Returns a codec for {@code schemaNode} using the registry default convention. */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode) {
        return getInstance(schemaNode, YangProtoConventionRegistry.getDefault());
    }

    /** @deprecated Use {@link #getInstance(SchemaNode, YangProtoConvention)}. */
    @Deprecated
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode, ProtoCodecMode mode) {
        return getInstance(schemaNode, mode, null, null);
    }

    /** Returns a codec for {@code schemaNode} using the given convention. */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        YangProtoConvention convention) {
        return getInstance(schemaNode, convention, null, null);
    }

    /** Returns a codec for {@code schemaNode} using the named convention. */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        String conventionName) {
        YangProtoConvention c = YangProtoConventionRegistry.get(conventionName);
        if (c == null) throw new IllegalArgumentException(
                "Convention '" + conventionName + "' is not registered.");
        return getInstance(schemaNode, c);
    }

    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        YangProtoConvention convention,
                                                        AnydataValidationContextResolver resolver,
                                                        String sourcePath) {
        if (schemaNode == null) return null;
        if (convention == null) convention = YangProtoConventionRegistry.getDefault();
        YangDataProtoCodec<?, ?> codec = createCodec(schemaNode, convention);
        codec.setAnydataValidationContextResolver(resolver);
        codec.setSourcePath(sourcePath);
        return codec;
    }

    /** @deprecated Use {@link #getInstance(SchemaNode, YangProtoConvention, AnydataValidationContextResolver, String)}. */
    @Deprecated
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        ProtoCodecMode mode,
                                                        AnydataValidationContextResolver resolver,
                                                        String sourcePath) {
        if (schemaNode == null) return null;
        if (mode == null) mode = ProtoCodecMode.SIMPLE;
        YangProtoConvention c = YangProtoConventionRegistry.get(mode.name().toLowerCase());
        if (c == null) c = YangProtoConventionRegistry.getDefault();
        return getInstance(schemaNode, c, resolver, sourcePath);
    }

    private static YangDataProtoCodec<?, ?> createCodec(SchemaNode schemaNode,
                                                          YangProtoConvention convention) {
        // Envelope mode — wraps with a single-field message (handled at a higher level)
        ProtoCodecMode legacyMode = "ygot".equals(convention.getName())
                ? ProtoCodecMode.YGOT : ProtoCodecMode.SIMPLE;

        if (schemaNode instanceof Container)
            return new ContainerDataProtoCodec((Container) schemaNode, legacyMode);
        if (schemaNode instanceof YangList)
            return new ListDataProtoCodec((YangList) schemaNode, legacyMode);
        if (schemaNode instanceof Leaf)
            return new LeafDataProtoCodec((Leaf) schemaNode, legacyMode);
        if (schemaNode instanceof LeafList)
            return new LeafListDataProtoCodec((LeafList) schemaNode, legacyMode);
        if (schemaNode instanceof Anydata)
            return new AnyDataDataProtoCodec((Anydata) schemaNode, legacyMode);
        if (schemaNode instanceof Anyxml)
            return new AnyxmlDataProtoCodec((Anyxml) schemaNode, legacyMode);
        if (schemaNode instanceof Notification)
            return new NotificationDataProtoCodec((Notification) schemaNode, legacyMode);
        if (schemaNode instanceof YangStructure)
            return new YangStructureDataProtoCodec((YangStructure) schemaNode, legacyMode);
        if (schemaNode instanceof Rpc)
            return new RpcDataProtoCodec((Rpc) schemaNode, legacyMode);
        if (schemaNode instanceof Input)
            return new InputDataProtoCodec((Input) schemaNode, legacyMode);
        if (schemaNode instanceof Output)
            return new OutputDataProtoCodec((Output) schemaNode, legacyMode);
        if (schemaNode instanceof Action)
            return new ActionDataProtoCodec((Action) schemaNode, legacyMode);
        throw new IllegalArgumentException(
                "Unsupported schema node type: " + schemaNode.getClass().getSimpleName());
    }

    protected AnydataValidationContextResolver getAnydataValidationContextResolver() {
        return anydataValidationContextResolver;
    }

    protected void setAnydataValidationContextResolver(AnydataValidationContextResolver anydataValidationContextResolver) {
        this.anydataValidationContextResolver = anydataValidationContextResolver;
    }

    protected String getSourcePath() {
        return sourcePath;
    }

    protected void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    // =========================================================================
    // Codec contract
    // =========================================================================

    @Override
    public T deserialize(DynamicMessage message,
                         ValidatorResultBuilder validatorResultBuilder) {
        if (message == null) return null;
        return buildData(message, validatorResultBuilder);
    }

    public T deserialize(DynamicMessage message,
                         ValidatorResultBuilder validatorResultBuilder,
                         AnydataValidationContextResolver resolver) {
        if (message == null) return null;
        this.anydataValidationContextResolver = resolver;
        return buildData(message, validatorResultBuilder);
    }

    public T deserialize(DynamicMessage message,
                         ValidatorResultBuilder validatorResultBuilder,
                         AnydataValidationOptions options) {
        return deserialize(message, validatorResultBuilder, (AnydataValidationContextResolver) options);
    }

    /**
     * Converts a protobuf {@link DynamicMessage} to a YANG data instance.
     * Implemented by each concrete codec.
     */
    protected abstract T buildData(DynamicMessage message,
                                   ValidatorResultBuilder validatorResultBuilder);

    /**
     * Returns a protobuf {@link Message.Builder} pre-configured with the
     * correct descriptor for this schema node.
     * Implemented by each concrete codec; the base implementation returns
     * {@code null} (subclasses must override).
     */
    protected Message.Builder buildElement(YangData<?> yangData) {
        return null;
    }

    @Override
    public DynamicMessage serialize(YangData<?> yangData) {
        Message.Builder builder = buildElement(yangData);
        if (builder == null) {
            throw new RuntimeException(
                    "No message builder for: " + schemaNode.getIdentifier());
        }
        if (yangData instanceof YangDataContainer) {
            ProtoCodecUtil.serializeChildren(builder, (YangDataContainer) yangData, mode);
        }        return (DynamicMessage) builder.build();
    }

    // =========================================================================
    // Shared helper for concrete subclasses
    // =========================================================================

    /**
     * Returns the {@link Descriptors.Descriptor} for this schema node using
     * the current codec mode.
     */
    protected Descriptors.Descriptor getDescriptorForNode() {
        return ProtoDescriptorManager.getInstance(convention.getName())
                .getDescriptor(schemaNode);
    }

    /** Returns the convention used by this codec. */
    public YangProtoConvention getConvention() { return convention; }
}
