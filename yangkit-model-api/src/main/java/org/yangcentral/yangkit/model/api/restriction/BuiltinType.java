package org.yangcentral.yangkit.model.api.restriction;

import java.util.ArrayList;

public enum BuiltinType {
   BINARY("binary"),
   BITS("bits"),
   BOOLEAN("boolean"),
   DECIMAL64("decimal64"),
   EMPTY("empty"),
   ENUMERATION("enumeration"),
   IDENTITYREF("identityref"),
   INSTANCEIDENTIFIER("instance-identifier"),
   INT8("int8"),
   INT16("int16"),
   INT32("int32"),
   INT64("int64"),
   LEAFREF("leafref"),
   STRING("string"),
   UINT8("uint8"),
   UINT16("uint16"),
   UINT32("uint32"),
   UINT64("uint64"),
   UNION("union");

   private String fieldName;

   private BuiltinType(String name) {
      this.fieldName = name;
   }

   public String getFieldName() {
      return this.fieldName;
   }

   public static boolean isBuiltinType(String name) {
      if (null == name) {
         return false;
      } else {
         return name.equals(BINARY.getFieldName())
                 || name.equals(BITS.getFieldName())
                 || name.equals(BOOLEAN.getFieldName())
                 || name.equals(DECIMAL64.getFieldName())
                 || name.equals(EMPTY.getFieldName())
                 || name.equals(ENUMERATION.getFieldName())
                 || name.equals(IDENTITYREF.getFieldName())
                 || name.equals(INSTANCEIDENTIFIER.getFieldName())
                 || name.equals(INT8.getFieldName())
                 || name.equals(INT16.getFieldName())
                 || name.equals(INT32.getFieldName())
                 || name.equals(INT64.getFieldName())
                 || name.equals(LEAFREF.getFieldName())
                 || name.equals(STRING.getFieldName())
                 || name.equals(UINT8.getFieldName())
                 || name.equals(UINT16.getFieldName())
                 || name.equals(UINT32.getFieldName())
                 || name.equals(UINT64.getFieldName())
                 || name.equals(UNION.getFieldName());
      }
   }

   public static BuiltinType getBuiltinType(String name) {
      if (null == name) {
         return null;
      } else if (name.equals(BINARY.getFieldName())) {
         return BINARY;
      } else if (name.equals(BITS.getFieldName())) {
         return BITS;
      } else if (name.equals(BOOLEAN.getFieldName())) {
         return BOOLEAN;
      } else if (name.equals(DECIMAL64.getFieldName())) {
         return DECIMAL64;
      } else if (name.equals(EMPTY.getFieldName())) {
         return EMPTY;
      } else if (name.equals(ENUMERATION.getFieldName())) {
         return ENUMERATION;
      } else if (name.equals(IDENTITYREF.getFieldName())) {
         return IDENTITYREF;
      } else if (name.equals(INSTANCEIDENTIFIER.getFieldName())) {
         return INSTANCEIDENTIFIER;
      } else if (name.equals(INT8.getFieldName())) {
         return INT8;
      } else if (name.equals(INT16.getFieldName())) {
         return INT16;
      } else if (name.equals(INT32.getFieldName())) {
         return INT32;
      } else if (name.equals(INT64.getFieldName())) {
         return INT64;
      } else if (name.equals(LEAFREF.getFieldName())) {
         return LEAFREF;
      } else if (name.equals(STRING.getFieldName())) {
         return STRING;
      } else if (name.equals(UINT8.getFieldName())) {
         return UINT8;
      } else if (name.equals(UINT16.getFieldName())) {
         return UINT16;
      } else if (name.equals(UINT32.getFieldName())) {
         return UINT32;
      } else if (name.equals(UINT64.getFieldName())) {
         return UINT64;
      } else {
         return name.equals(UNION.getFieldName()) ? UNION : null;
      }
   }

   public static ArrayList<String> getBuiltinTypes() {
      BuiltinType[] bts = values();
      ArrayList<String> btl = null;
      int size = bts.length;

      for(int i = 0; i < size; ++i) {
         BuiltinType bt = bts[i];
         if (null != bt) {
            if (null == btl) {
               btl = new ArrayList(1);
            }

            btl.add(bt.getFieldName());
         }
      }

      return btl;
   }

   public static boolean isIntegerType(BuiltinType t) {
      if (null == t) {
         return false;
      } else {
         switch (t) {
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
               return true;
            default:
               return false;
         }
      }
   }
}
