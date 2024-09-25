package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.impl.model.NotificationMessageImpl;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.Iterator;
import java.util.Map;

public class NotificationMessageJsonCodec extends YangStructureMessageJsonCodec<NotificationMessage> {


    private String fi;

    public NotificationMessageJsonCodec(YangSchemaContext schemaContext) {
        super((YangStructure) schemaContext.getSchemaNodeChild(new QName("urn:ietf:params:xml:ns:netconf:notification:1.0",
                "notification")),schemaContext);
    }

    @Override
    protected void parseContent(JsonNode document, NotificationMessage m,ValidatorResultBuilder builder) {
        String structureName = getStructure().getJsonIdentifier();
        JsonNode structureNode = document.get(structureName);
        Iterator<String> fields = structureNode.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            JsonNode fieldNode = structureNode.get(field);
            QName qName = JsonCodecUtil.getQNameFromJsonField(field,
                    (YangDataContainer) m.getStructureData().getDataChildren().get(0));
            if(qName == null) {
                continue;
            }
            SchemaNode schemaNode = getStructure().getTreeNodeChild(qName);
            if(schemaNode != null){
                continue;
            }
            SchemaNode contentSchemaNode = getSchemaContext().getTreeNodeChild(qName);
            if(!(contentSchemaNode instanceof Notification)) {
                ValidatorRecordBuilder<String,JsonNode> validatorRecordBuilder = new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setBadElement(fieldNode);
                validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                validatorRecordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(fieldNode));
                validatorRecordBuilder.setErrorMessage(new ErrorMessage("wrong data, it should be notification data"));
                builder.addRecord(validatorRecordBuilder.build());
                continue;
            }
            YangDataDocument body = new YangDataDocumentImpl(null,getSchemaContext(), document.toString());
            builder.merge(JsonCodecUtil.buildChildData(body,fieldNode,contentSchemaNode));
            m.setBody(body);
            return;
        }

    }

    @Override
    protected NotificationMessage newStructureInstance() {
        return new NotificationMessageImpl(this.getStructure());
    }

    @Override
    public NotificationMessage deserialize(JsonNode document, ValidatorResultBuilder builder) {
        return super.deserialize(document,builder);

    }

    @Override
    public JsonNode serialize(NotificationMessage yangDataMessage) {
        YangDataDocumentJsonCodec structureDocJsonCodec = new YangDataDocumentJsonCodec(getSchemaContext());
        ObjectNode structureDoc = (ObjectNode) structureDocJsonCodec.serialize(yangDataMessage.getStructureData());
        ObjectNode structureNode = null;
        Iterator<String> fieldNames = structureDoc.fieldNames();
        while (fieldNames.hasNext()){
            String fieldName = fieldNames.next();
            structureNode = (ObjectNode) structureDoc.get(fieldName);
            break;
        }
        if(null == structureNode) {
            return null;
        }

        ObjectNode contentNode = (ObjectNode) super.serialize(yangDataMessage);
        Iterator<Map.Entry<String, JsonNode>> fields = contentNode.fields();
        while (fields.hasNext()){
            Map.Entry<String, JsonNode> field = fields.next();
            structureNode.put(field.getKey(), field.getValue());
        }
        return structureDoc;
    }
}
