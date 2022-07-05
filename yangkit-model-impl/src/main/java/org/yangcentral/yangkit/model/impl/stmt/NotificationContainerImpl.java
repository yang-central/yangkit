package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.NotificationContainer;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class NotificationContainerImpl implements NotificationContainer {
   private List<Notification> notifications = new ArrayList();
   private YangContext yangContext;

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public List<Notification> getNotifications() {
      return Collections.unmodifiableList(this.notifications);
   }

   public Notification getNotification(String name) {
      Iterator var2 = this.notifications.iterator();

      Notification notification;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         notification = (Notification)var2.next();
      } while(!notification.getArgStr().equals(name));

      return notification;
   }

   public ValidatorResult addNotification(Notification notification) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      SchemaNode schemaNode = (SchemaNode)this.getYangContext().getSchemaNodeIdentifierCache().get(notification.getArgStr());
      if (null != schemaNode) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(schemaNode, notification));
         notification.setErrorStatement(true);
         return validatorResultBuilder.build();
      } else {
         this.notifications.add(notification);
         this.getYangContext().getSchemaNodeIdentifierCache().put(notification.getArgStr(), notification);
         return validatorResultBuilder.build();
      }
   }
}
