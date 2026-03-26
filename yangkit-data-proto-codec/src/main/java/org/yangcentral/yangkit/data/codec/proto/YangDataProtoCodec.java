package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.List;

/**
 * Abstract base class for YANG data to Protocol Buffers codec.
 * Provides bidirectional conversion between YANG data and protobuf messages.
 * 
 * @param <S> Schema node type
 * @param <T> YANG data type
 */
public abstract class YangDataProtoCodec<S extends SchemaNode, T extends YangData<S>> implements YangDataCodec<S, T, DynamicMessage> {

    private S schemaNode;

    protected YangDataProtoCodec(S schemaNode) {
        this.schemaNode = schemaNode;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaNode.getContext().getSchemaContext();
    }

    @Override
    public S getSchemaNode() {
        return schemaNode;
    }

    /**
     * Get instance of appropriate codec based on schema node type.
     * 
     * @param dataSchemaNode the schema node
     * @return corresponding codec instance
     */
    public static YangDataProtoCodec<?, ?> getInstance(SchemaNode dataSchemaNode) {
        if (null == dataSchemaNode) {
            return null;
        }
        if (dataSchemaNode instanceof Container) {
            return new ContainerDataProtoCodec((Container) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangList) {
            return new ListDataProtoCodec((YangList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Leaf) {
            return new LeafDataProtoCodec((Leaf) dataSchemaNode);
        } else if (dataSchemaNode instanceof LeafList) {
            return new LeafListDataProtoCodec((LeafList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anydata) {
            return new AnyDataDataProtoCodec((Anydata) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anyxml) {
            return new AnyxmlDataProtoCodec((Anyxml) dataSchemaNode);
        } else if (dataSchemaNode instanceof Notification) {
            return new NotificationDataProtoCodec((Notification) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangStructure) {
            return new YangStructureDataProtoCodec((YangStructure) dataSchemaNode);
        } else if (dataSchemaNode instanceof Rpc) {
            return new RpcDataProtoCodec((Rpc) dataSchemaNode);
        } else if (dataSchemaNode instanceof Input) {
            return new InputDataProtoCodec((Input) dataSchemaNode);
        } else if (dataSchemaNode instanceof Output) {
            return new OutputDataProtoCodec((Output) dataSchemaNode);
        } else if (dataSchemaNode instanceof Action) {
            return new ActionDataProtoCodec((Action) dataSchemaNode);
        } else {
            throw new IllegalArgumentException("not-support data schema type");
        }
    }

    @Override
    public T deserialize(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder) {
        if (null == message) {
            return null;
        }
        T data = buildData(message, validatorResultBuilder);
        return data;
    }

    /**
     * Build YANG data from protobuf message.
     * 
     * @param message the protobuf message
     * @param validatorResultBuilder validator result builder
     * @return YANG data instance
     */
    abstract protected T buildData(DynamicMessage message, ValidatorResultBuilder validatorResultBuilder);

    /**
     * Build protobuf message builder from YANG data.
     * 
     * @param yangData the YANG data
     * @return protobuf message builder
     */
    protected Message.Builder buildElement(YangData<?> yangData) {
        // Implementation will be in concrete classes
        return null;
    }

    @Override
    public DynamicMessage serialize(YangData<?> yangData) {
        List<Attribute> attributes = yangData.getAttributes();
        Message.Builder builder = buildElement(yangData);
        
        if (yangData instanceof YangDataContainer) {
            ProtoCodecUtil.serializeChildren(builder, (YangDataContainer) yangData);
        }

        return (DynamicMessage) builder.build();
    }
}
