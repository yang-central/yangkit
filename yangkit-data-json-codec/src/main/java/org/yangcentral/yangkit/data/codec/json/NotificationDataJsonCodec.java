package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Notification;

class NotificationDataJsonCodec extends YangDataJsonCodec<Notification, NotificationData> {
    protected NotificationDataJsonCodec(Notification schemaNode) {
        super(schemaNode);
    }

    @Override
    protected NotificationData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        NotificationData notificationData = (NotificationData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        return notificationData;
    }


}