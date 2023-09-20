package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangStructureMessage;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

import java.util.Iterator;

public abstract class YangDataStructureMessageJsonCodec<T extends YangStructureMessage<T>> extends YangDataMessageJsonCodec<T> {
    private YangDataStructure structure;

    public YangDataStructure getStructure() {
        return structure;
    }

    public YangDataStructureMessageJsonCodec(YangDataStructure structure, YangSchemaContext schemaContext) {
        super(schemaContext);
        this.structure = structure;
    }

    protected abstract T newStructureInstance();

    @Override
    protected T parseHeader(JsonNode document, ValidatorResultBuilder builder) {
        String structureName = structure.getJsonIdentifier();
        Iterator<String> fieldNames = document.fieldNames();
        boolean hasError = false;
        while (fieldNames.hasNext()){
            String fieldName = fieldNames.next();
            if(!fieldName.equals(structureName)){
                ValidatorRecordBuilder<String,JsonNode> validatorRecordBuilder = new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setBadElement(document.get(fieldName));
                validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                validatorRecordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(document.get(fieldName)));
                validatorRecordBuilder.setErrorMessage(new ErrorMessage("unrecognized field:"+ fieldName));
                builder.addRecord(validatorRecordBuilder.build());
                hasError = true;
            }
        }
        if (hasError) {
            return null;
        }
        T instance = newStructureInstance();
        YangDataDocument yangDataDocument = new YangDataDocumentImpl(structure.getIdentifier(), this.getSchemaContext());
        instance.setDocument(yangDataDocument);
        JsonNode structureNode = document.get(structureName);
        Iterator<String> fields = structureNode.fieldNames();
        while (fields.hasNext()){
            String field = fields.next();
            JsonNode fieldNode = structureNode.get(field);
            //check the field name according to structure
            QName qName = JsonCodecUtil.getQNameFromJsonField(field,getSchemaContext());
            if(qName == null) {
                ValidatorRecordBuilder<String,JsonNode> validatorRecordBuilder = new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setBadElement(fieldNode);
                validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                validatorRecordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(fieldNode));
                validatorRecordBuilder.setErrorMessage(new ErrorMessage("unrecognized field:"+ field));
                builder.addRecord(validatorRecordBuilder.build());
                continue;
            }
            SchemaNode dataNode = structure.getTreeNodeChild(qName);
            if(dataNode != null) {
                YangDataJsonCodec yangDataJsonCodec = YangDataJsonCodec.getInstance(dataNode);
                YangData<?> fieldData = yangDataJsonCodec.deserialize(fieldNode,builder);
                try {
                    yangDataDocument.addChild(fieldData);
                } catch (YangDataException e) {
                    ValidatorRecordBuilder<String,JsonNode> validatorRecordBuilder = new ValidatorRecordBuilder<>();
                    validatorRecordBuilder.setBadElement(fieldNode);
                    validatorRecordBuilder.setErrorTag(e.getErrorTag());
                    validatorRecordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(fieldNode));
                    validatorRecordBuilder.setErrorMessage(new ErrorMessage(e.getMessage()));
                    builder.addRecord(validatorRecordBuilder.build());
                }
            }
        }
        return instance;
    }

    @Override
    public T deserialize(JsonNode document, ValidatorResultBuilder builder) {


        return super.deserialize(document, builder);
    }

    @Override
    public JsonNode serialize(T yangDataMessage) {
        return super.serialize(yangDataMessage);
    }
}
