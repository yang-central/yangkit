package org.yangcentral.yangkit.model.api.stmt;

public enum DeviateType {
   ADD("add"),
   DELETE("delete"),
   REPLACE("replace"),
   NOT_SUPPORTED("not-supported");

   private String value;

   private DeviateType(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   public static DeviateType forValue(String value) {
      switch (value) {
         case "add":
            return ADD;
         case "delete":
            return DELETE;
         case "replace":
            return REPLACE;
         case "not-supported":
            return NOT_SUPPORTED;
         default:
            throw new IllegalArgumentException("Unexpected value: " + value);
      }
   }
}
