package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.List;

public abstract class YangDataJsonCodec<S extends SchemaNode, T extends YangData<S>> implements YangDataCodec<S, T, JsonNode> {

    private S schemaNode;
    private AnydataValidationContextResolver anydataValidationContextResolver;
    private String sourcePath;

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
        return getInstance(dataSchemaNode, null, null);
    }

    public static YangDataJsonCodec<?, ?> getInstance(SchemaNode dataSchemaNode,
                                                       AnydataValidationContextResolver resolver,
                                                       String sourcePath) {
        if (null == dataSchemaNode) {
            return null;
        }
        YangDataJsonCodec<?, ?> codec;
        if (dataSchemaNode instanceof Container) {
            codec = new ContainerDataJsonCodec((Container) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangList) {
            codec = new ListDataJsonCodec((YangList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Leaf) {
            codec = new LeafDataJsonCodec((Leaf) dataSchemaNode);
        } else if (dataSchemaNode instanceof LeafList) {
            codec = new LeafListDataJsonCodec((LeafList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anydata) {
            codec = new AnyDataDataJsonCodec((Anydata) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anyxml) {
            codec = new AnyxmlDataJsonCodec((Anyxml) dataSchemaNode);
        } else if (dataSchemaNode instanceof Notification) {
            codec = new NotificationDataJsonCodec((Notification) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangStructure) {
            codec = new YangStructureDataJsonCodec((YangStructure) dataSchemaNode);
        } else if (dataSchemaNode instanceof Rpc) {
            codec = new RpcDataJsonCodec((Rpc) dataSchemaNode);
        } else if (dataSchemaNode instanceof Input) {
            codec = new InputDataJsonCodec((Input) dataSchemaNode);
        } else if (dataSchemaNode instanceof Output) {
            codec = new OutputDataJsonCodec((Output) dataSchemaNode);
        } else if (dataSchemaNode instanceof Action) {
            codec = new ActionDataJsonCodec((Action) dataSchemaNode);
        } else {
            throw new IllegalArgumentException("not-support data schema type");
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


    @Override
    public T deserialize(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        if (null == element) {
            return null;
        }
        
        // Create and set validation context for JSON path generation
        ExtraValidationDataJsonCodec validationContext = new ExtraValidationDataJsonCodec();
        ExtraValidationDataContext.setCurrentContext(validationContext);
        
        try {
            T data = buildData(element, validatorResultBuilder);
            return data;
        } finally {
            // Clear context after deserialization to prevent memory leaks
            ExtraValidationDataContext.clearContext();
        }
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
