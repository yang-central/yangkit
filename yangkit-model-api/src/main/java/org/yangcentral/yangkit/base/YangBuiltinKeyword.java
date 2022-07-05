package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.QName;

public enum YangBuiltinKeyword {
   ACTION("action", "name", false),
   ANYXML("anyxml", "name", false),
   ANYDATA("anydata", "name", false),
   ARGUMENT("argument", "name", false),
   AUGMENT("augment", "target-node", false),
   BASE("base", "name", false),
   BELONGSTO("belongs-to", "module", false),
   BIT("bit", "name", false),
   CASE("case", "name", false),
   CHOICE("choice", "name", false),
   CONFIG("config", "value", false),
   CONTACT("contact", "text", true),
   CONTAINER("container", "name", false),
   DEFAULT("default", "value", false),
   DESCRIPTION("description", "text", true),
   ENUM("enum", "name", false),
   ERRORAPPTAG("error-app-tag", "value", false),
   ERRORMESSAGE("error-message", "value", true),
   MODIFIER("modifier", "value", false),
   EXTENSION("extension", "name", false),
   DEVIATION("deviation", "target-node", false),
   DEVIATE("deviate", "value", false),
   FEATURE("feature", "name", false),
   FRACTIONDIGITS("fraction-digits", "value", false),
   GROUPING("grouping", "name", false),
   IDENTITY("identity", "name", false),
   IFFEATURE("if-feature", "name", false),
   INCLUDE("include", "module", false),
   IMPORT("import", "module", false),
   INPUT("input"),
   KEY("key", "value", false),
   LEAF("leaf", "name", false),
   LEAFLIST("leaf-list", "name", false),
   LENGTH("length", "value", false),
   LIST("list", "name", false),
   MANDATORY("mandatory", "value", false),
   MAXELEMENTS("max-elements", "value", false),
   MINELEMENTS("min-elements", "value", false),
   MODULE("module", "name", false),
   MUST("must", "condition", false),
   NAMESPACE("namespace", "uri", false),
   NOTIFICATION("notification", "name", false),
   ORDEREDBY("ordered-by", "value", false),
   ORGANIZATION("organization", "text", true),
   OUTPUT("output"),
   PATH("path", "value", false),
   PATTERN("pattern", "value", false),
   POSITION("position", "value", false),
   PREFIX("prefix", "value", false),
   PRESENCE("presence", "value", false),
   RANGE("range", "value", false),
   REFERENCE("reference", "text", true),
   REFINE("refine", "target-node", false),
   REQUIREINSTANCE("require-instance", "value", false),
   REVISION("revision", "date", false),
   REVISIONDATE("revision-date", "date", false),
   RPC("rpc", "name", false),
   STATUS("status", "value", false),
   SUBMODULE("submodule", "name", false),
   TYPE("type", "name", false),
   TYPEDEF("typedef", "name", false),
   UNIQUE("unique", "tag", false),
   UNITS("units", "name", false),
   USES("uses", "name", false),
   VALUE("value", "value", false),
   WHEN("when", "condition", false),
   YANGVERSION("yang-version", "value", false),
   YINELEMENT("yin-element", "value", false);

   private String keyword;
   private String argument;
   private boolean isYinElement;

   public String getKeyword() {
      return this.keyword;
   }

   public boolean isYinElement() {
      return this.isYinElement;
   }

   public String getArgument() {
      return this.argument;
   }

   public QName getQName() {
      return new QName(Yang.NAMESPACE, this.keyword);
   }

   public static boolean isKeyword(String keyword) {
      YangBuiltinKeyword[] keys = values();
      YangBuiltinKeyword[] var2 = keys;
      int var3 = keys.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         YangBuiltinKeyword key = var2[var4];
         if (key.getKeyword().equals(keyword)) {
            return true;
         }
      }

      return false;
   }

   public static YangBuiltinKeyword from(QName keyword) {
      return !keyword.getNamespace().equals(Yang.NAMESPACE.getUri()) ? null : getYangKeyword(keyword.getLocalName());
   }

   public static YangBuiltinKeyword getYangKeyword(String keyword) {
      YangBuiltinKeyword[] keys = values();
      YangBuiltinKeyword[] var2 = keys;
      int var3 = keys.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         YangBuiltinKeyword key = var2[var4];
         if (key.getKeyword().equals(keyword)) {
            return key;
         }
      }

      return null;
   }

   private YangBuiltinKeyword() {
   }

   private YangBuiltinKeyword(String keyword) {
      this.keyword = keyword;
   }

   private YangBuiltinKeyword(String keyword, String argument, boolean isYinElement) {
      this.keyword = keyword;
      this.argument = argument;
      this.isYinElement = isYinElement;
   }
}
