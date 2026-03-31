package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.*;
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
    private AnydataValidationContextResolver anydataValidationContextResolver;
    private String sourcePath;

    protected YangDataProtoCodec(S schemaNode, ProtoCodecMode mode) {
        this.schemaNode = schemaNode;
        this.mode       = mode;
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

    /**
     * Returns a codec for {@code schemaNode} using {@link ProtoCodecMode#SIMPLE}.
     */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode) {
        return getInstance(schemaNode, ProtoCodecMode.SIMPLE);
    }

    /**
     * Returns a codec for {@code schemaNode} using the given {@code mode}.
     *
     * @throws IllegalArgumentException if the schema node type is not supported
     */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        ProtoCodecMode mode) {
        return getInstance(schemaNode, mode, null, null);
    }

    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode schemaNode,
                                                        ProtoCodecMode mode,
                                                        AnydataValidationContextResolver resolver,
                                                        String sourcePath) {
        if (schemaNode == null) return null;
        if (mode == null) mode = ProtoCodecMode.SIMPLE;

        YangDataProtoCodec<?, ?> codec;
        if (schemaNode instanceof Container) {
            codec = new ContainerDataProtoCodec((Container) schemaNode, mode);
        } else if (schemaNode instanceof YangList) {
            codec = new ListDataProtoCodec((YangList) schemaNode, mode);
        } else if (schemaNode instanceof Leaf) {
            codec = new LeafDataProtoCodec((Leaf) schemaNode, mode);
        } else if (schemaNode instanceof LeafList) {
            codec = new LeafListDataProtoCodec((LeafList) schemaNode, mode);
        } else if (schemaNode instanceof Anydata) {
            codec = new AnyDataDataProtoCodec((Anydata) schemaNode, mode);
        } else if (schemaNode instanceof Anyxml) {
            codec = new AnyxmlDataProtoCodec((Anyxml) schemaNode, mode);
        } else if (schemaNode instanceof Notification) {
            codec = new NotificationDataProtoCodec((Notification) schemaNode, mode);
        } else if (schemaNode instanceof YangStructure) {
            codec = new YangStructureDataProtoCodec((YangStructure) schemaNode, mode);
        } else if (schemaNode instanceof Rpc) {
            codec = new RpcDataProtoCodec((Rpc) schemaNode, mode);
        } else if (schemaNode instanceof Input) {
            codec = new InputDataProtoCodec((Input) schemaNode, mode);
        } else if (schemaNode instanceof Output) {
            codec = new OutputDataProtoCodec((Output) schemaNode, mode);
        } else if (schemaNode instanceof Action) {
            codec = new ActionDataProtoCodec((Action) schemaNode, mode);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported schema node type: " + schemaNode.getClass().getSimpleName());
        }
        codec.setAnydataValidationContextResolver(resolver);
        codec.setSourcePath(sourcePath);
        return codec;
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
        }
        return (DynamicMessage) builder.build();
    }

    // =========================================================================
    // Shared helper for concrete subclasses
    // =========================================================================

    /**
     * Returns the {@link Descriptors.Descriptor} for this schema node using
     * the current codec mode.
     */
    protected Descriptors.Descriptor getDescriptorForNode() {
        return ProtoDescriptorManager.getInstance(mode).getDescriptor(schemaNode);
    }
}
