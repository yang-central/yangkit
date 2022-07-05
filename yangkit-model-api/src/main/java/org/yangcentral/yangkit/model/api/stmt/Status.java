package org.yangcentral.yangkit.model.api.stmt;

public enum Status {
   CURRENT("current"),
   DEPRECATED("deprecated"),
   OBSOLETE("obsolete");

   private String status;

   private Status(String status) {
      this.status = status;
   }

   public String getStatus() {
      return this.status;
   }

   public static Status getStatus(String status) {
      if (null == status) {
         return null;
      } else if ("current".equals(status)) {
         return CURRENT;
      } else if ("deprecated".equals(status)) {
         return DEPRECATED;
      } else {
         return "obsolete".equals(status) ? OBSOLETE : null;
      }
   }
}
