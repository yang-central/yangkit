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

    abstract protected void buildElement(JsonNode element, YangData<?> yangData);


    protected void serializeChildren(JsonNode element, YangDataContainer yangDataContainer) {
        SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) (((YangData) yangDataContainer).getSchemaNode());
        List<SchemaNode> schemaChildren = schemaNodeContainer.getTreeNodeChildren();
        if (schemaChildren.isEmpty()) {
            return;
        }
        for (SchemaNode dataChild : schemaChildren) {
            List<YangData<?>> childData = yangDataContainer
                    .getDataChildren(dataChild.getIdentifier());
            for (YangData<?> childDatum : childData) {
                if (childDatum.isDummyNode()) {
                    continue;
                }
                YangDataJsonCodec jsonCodec = getInstance(childDatum.getSchemaNode());
                JsonNode childElement = jsonCodec.serialize(childDatum);
                String moduleName = childDatum.getSchemaNode().getContext().getCurModule().getMainModule().getArgStr();
                ((ObjectNode) element).put(moduleName + ":" + childDatum.getQName().getLocalName(), childElement);

            }
        }
    }

    @Override
    public JsonNode serialize(YangData<?> yangData) {
        List<Attribute> list = yangData.getAttributes();
        if (list.size() > 0) {
            System.out.println("Datajsoncodec");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode element = objectMapper.createObjectNode();
        if (yangData instanceof LeafData || yangData instanceof LeafListData) {

            String value = ((TypedData) yangData).getStringValue();
            LeafSetValue temp = new LeafSetValue(new Object());
            temp.setValue(value);
            element = temp;
        }

        buildElement(element, yangData);
        if (yangData instanceof YangDataContainer) {
            serializeChildren(element, (YangDataContainer) yangData);
        }

        return element;
    }


}
