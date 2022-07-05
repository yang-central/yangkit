package org.yangcentral.yangkit.model.api.stmt;

public enum OrderBy {
   SYSTEM("system"),
   USER("user");

   private String orderBy;

   private OrderBy(String orderBy) {
      this.orderBy = orderBy;
   }

   public String getOrderBy() {
      return this.orderBy;
   }

   public static OrderBy getOrderBy(String orderBy) {
      if (null == orderBy) {
         return null;
      } else if ("system".equals(orderBy)) {
         return SYSTEM;
      } else {
         return "user".equals(orderBy) ? USER : null;
      }
   }

   public String toString() {
      return this.orderBy;
   }
}
