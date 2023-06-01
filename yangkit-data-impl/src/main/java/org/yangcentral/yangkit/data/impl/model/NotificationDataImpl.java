package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.model.api.stmt.Notification;

public class NotificationDataImpl extends YangDataContainerImpl<Notification> implements NotificationData {
    public NotificationDataImpl(Notification schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }
}
