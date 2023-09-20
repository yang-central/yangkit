package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.List;

public abstract class YangDataJsonCodec<S extends SchemaNode, T extends YangData<S>> implements YangDataCodec<S, T, JsonNode> {

    private S schemaNode;

    protected YangDataJsonCodec(S schemaNode) {
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

    public static YangDataJsonCodec<?, ?> getInstance(SchemaNode dataSchemaNode) {
        if (null == dataSchemaNode) {
            return null;
        }
        if (dataSchemaNode instanceof Container) {
            return new ContainerDataJsonCodec((Container) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangList) {
            return new ListDataJsonCodec((YangList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Leaf) {
            return new LeafDataJsonCodec((Leaf) dataSchemaNode);
        } else if (dataSchemaNode instanceof LeafList) {
            return new LeafListDataJsonCodec((LeafList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anydata) {
            return new AnyDataDataJsonCodec((Anydata) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anyxml) {
            return new AnyxmlDataJsonCodec((Anyxml) dataSchemaNode);
        } else if (dataSchemaNode instanceof Notification) {
            return new NotificationDataJsonCodec((Notification) dataSchemaNode);
        } else {
            throw new IllegalArgumentException("not-support data schema type");
        }
    }


    @Override
    public T deserialize(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        if (null == element) {
            return null;
        }
        T data = buildData(element, validatorResultBuilder);
        return data;
    }

    abstract protected T buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder);

    protected JsonNode buildElement(YangData<?> yangData){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }




    @Override
    public JsonNode serialize(YangData<?> yangData) {
        List<Attribute> list = yangData.getAttributes();

        JsonNode element = buildElement(yangData);
        if (yangData instanceof YangDataContainer) {
            if(element.isObject()){
                JsonCodecUtil.serializeChildren((ObjectNode) element, (YangDataContainer) yangData);
            }

        }

        return element;
    }


}
