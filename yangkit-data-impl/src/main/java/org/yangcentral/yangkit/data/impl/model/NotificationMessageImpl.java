package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.List;

public class NotificationMessageImpl extends YangStructureMessageImpl<NotificationMessage> implements NotificationMessage {
    public NotificationMessageImpl(YangStructure structure) {
        super(structure);
    }

    @Override
    public NotificationData getNotificationData() {
        List<YangData<?>> dataChildren = getBody().getDataChildren();
        for(YangData<?> child: dataChildren){
            if(child instanceof NotificationData){
                return (NotificationData) child;
            }
        }
        return null;
    }

}
