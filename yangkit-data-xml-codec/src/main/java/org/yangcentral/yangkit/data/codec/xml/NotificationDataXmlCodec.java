package org.yangcentral.yangkit.data.codec.xml;

import org.dom4j.Element;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Notification;
class NotificationDataXmlCodec extends YangDataXmlCodec<Notification, NotificationData> {
    protected NotificationDataXmlCodec(Notification schemaNode) {
        super(schemaNode);
    }

    @Override
    protected NotificationData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        NotificationData notificationData = (NotificationData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),null);
        return notificationData;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {

    }
}