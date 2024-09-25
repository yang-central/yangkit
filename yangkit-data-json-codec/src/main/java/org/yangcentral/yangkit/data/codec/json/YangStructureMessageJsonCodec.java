package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.data.api.model.YangStructureMessage;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.Iterator;

public abstract class YangStructureMessageJsonCodec<T extends YangStructureMessage<T>> extends YangDataMessageJsonCodec<T> {
    private YangStructure structure;

    public YangStructure getStructure() {
        return structure;
    }

    public YangStructureMessageJsonCodec(YangStructure structure, YangSchemaContext schemaContext) {
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
        JsonNode structureNode = document.get(structureName);
        YangDataDocument structureDoc = new YangDataDocumentImpl(null,getSchemaContext(), document.toString());
        YangStructureData yangStructureData = new YangStructureDataJsonCodec(structure).deserialize(structureNode,builder);
        if(!builder.build().isOk()) {
            return null;
        }
        try {
            structureDoc.addChild(yangStructureData);
        } catch (YangDataException e) {
            ValidatorRecordBuilder validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(e.getErrorTag());
            validatorRecordBuilder.setErrorMessage(e.getErrorMsg());
            validatorRecordBuilder.setBadElement(e.getBadElement());
            validatorRecordBuilder.setErrorPath(e.getErrorPath());
            builder.addRecord(validatorRecordBuilder.build());
            return null;
        }
        instance.setStructureData(structureDoc);
        Iterator<String> fields = structureNode.fieldNames();
        while (fields.hasNext()){
            String field = fields.next();
            JsonNode fieldNode = structureNode.get(field);
            //check the field name according to structure
            QName qName = JsonCodecUtil.getQNameFromJsonField(field,yangStructureData);
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
                JsonCodecUtil.buildChildData(yangStructureData,fieldNode,dataNode);
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
