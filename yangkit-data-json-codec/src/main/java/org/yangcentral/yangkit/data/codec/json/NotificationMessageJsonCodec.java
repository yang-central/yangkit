package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataMessageCodec;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataMessage;
import org.yangcentral.yangkit.data.impl.model.NotificationMessageImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

import java.util.Iterator;

public class NotificationMessageJsonCodec extends YangDataStructureMessageJsonCodec<NotificationMessage> {


    public NotificationMessageJsonCodec(YangSchemaContext schemaContext) {
        super((YangDataStructure) schemaContext.getSchemaNodeChild(new QName("urn:ietf:params:xml:ns:netconf:notification:1.0",
                "notification")),schemaContext);
    }

    @Override
    protected YangDataContainer parseContent(JsonNode document, ValidatorResultBuilder builder) {
        String structureName = getStructure().getJsonIdentifier();
        JsonNode structureNode = document.get(structureName);
        Iterator<String> fields = structureNode.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            JsonNode fieldNode = structureNode.get(field);
            QName qName = JsonCodecUtil.getQNameFromJsonField(field,getSchemaContext());
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
            NotificationDataJsonCodec notificationDataJsonCodec = new NotificationDataJsonCodec(
                    (Notification) contentSchemaNode);
            NotificationData notificationData = notificationDataJsonCodec.deserialize(fieldNode,builder);
            return notificationData;
        }
        return null;
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
        return super.serialize(yangDataMessage);
    }
}
