package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

import java.util.List;

public class NotificationMessageImpl extends YangStructureMessageImpl implements NotificationMessage {
    public NotificationMessageImpl(YangDataStructure structure) {
        super(structure);
    }

    @Override
    public NotificationData getNotificationData() {
        List<YangData<?>> dataChildren = getDocument().getDataChildren();
        for(YangData<?> child: dataChildren){
            if(child instanceof NotificationData){
                return (NotificationData) child;
            }
        }
        return null;
    }
}
