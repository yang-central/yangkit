package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
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
        if (schemaNode == null) return null;
        if (mode == null) mode = ProtoCodecMode.SIMPLE;

        if (schemaNode instanceof Container) {
            return new ContainerDataProtoCodec((Container) schemaNode, mode);
        } else if (schemaNode instanceof YangList) {
            return new ListDataProtoCodec((YangList) schemaNode, mode);
        } else if (schemaNode instanceof Leaf) {
            return new LeafDataProtoCodec((Leaf) schemaNode, mode);
        } else if (schemaNode instanceof LeafList) {
            return new LeafListDataProtoCodec((LeafList) schemaNode, mode);
        } else if (schemaNode instanceof Anydata) {
            return new AnyDataDataProtoCodec((Anydata) schemaNode, mode);
        } else if (schemaNode instanceof Anyxml) {
            return new AnyxmlDataProtoCodec((Anyxml) schemaNode, mode);
        } else if (schemaNode instanceof Notification) {
            return new NotificationDataProtoCodec((Notification) schemaNode, mode);
        } else if (schemaNode instanceof YangStructure) {
            return new YangStructureDataProtoCodec((YangStructure) schemaNode, mode);
        } else if (schemaNode instanceof Rpc) {
            return new RpcDataProtoCodec((Rpc) schemaNode, mode);
        } else if (schemaNode instanceof Input) {
            return new InputDataProtoCodec((Input) schemaNode, mode);
        } else if (schemaNode instanceof Output) {
            return new OutputDataProtoCodec((Output) schemaNode, mode);
        } else if (schemaNode instanceof Action) {
            return new ActionDataProtoCodec((Action) schemaNode, mode);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported schema node type: " + schemaNode.getClass().getSimpleName());
        }
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
            ProtoCodecUtil.serializeChildren(builder, (YangDataContainer) yangData);
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
