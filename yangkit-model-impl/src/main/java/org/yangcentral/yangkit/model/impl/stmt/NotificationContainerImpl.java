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
   private List<Notification> notifications = new ArrayList<>();
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
      Iterator<Notification> iterator = this.notifications.iterator();

      Notification notification;
      do {
         if (!iterator.hasNext()) {
            return null;
         }

         notification = iterator.next();
      } while(!notification.getArgStr().equals(name));

      return notification;
   }

   public Notification removeNotification(String name){
      Notification notification = (Notification) this.getYangContext().getSchemaNodeIdentifierCache().remove(name);
      if(notification == null){
         return null;
      }
      notifications.remove(notification);
      return notification;
   }

   public void removeNotifications(){
      for(Notification notification:notifications){
         this.getYangContext().getSchemaNodeIdentifierCache().remove(notification.getArgStr());
      }
      notifications.clear();
   }

   public ValidatorResult addNotification(Notification notification) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      SchemaNode schemaNode = this.getYangContext().getSchemaNodeIdentifierCache().get(notification.getArgStr());
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
