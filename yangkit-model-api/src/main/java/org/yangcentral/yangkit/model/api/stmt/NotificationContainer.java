package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface NotificationContainer {
   List<Notification> getNotifications();

   Notification getNotification(String name);

   ValidatorResult addNotification(Notification notification);
}
