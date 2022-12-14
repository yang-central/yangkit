package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.exception.Severity;
/**
 *
 * @version 1.0.0
 * @author frank feng
 * @since 7/7/2022
 */
public enum ErrorCode {
   ACTION_IN_DATATREE("action node MUST be tied in data tree,"),
   ACTION_IN_LIST_NO_KEY("action node's ancestor node contains list without key,"),
   ACTION_NOT_TOP("action MUST NOT be the top node of module"),
   AUGMENT_MANDATORY_NODE("augment mandatory node"),
   BIT_NO_POSITION(Severity.WARNING, "bit's position MUST be specified for current maximum position reaching max(4294967295)"),
   CARDINALITY_BROKEN("cardinality is broken."),
   CIRCLE_REFERNCE("circle reference occurs."),
   COMMON_ERROR("internal error."),
   CONFIG_CONFILICT("config attribute conflict, config true node MUST not be child of config false node."),
   CONFLICT_NAME("conflict name occurred."),
   DEFAULT_CASE_IS_MANDATORY(Severity.WARNING, "the default case has mandatory nodes."),
   DEFAULT_CASE_IS_OPTIONAL(Severity.WARNING, "the default case is an optional case, it may be caused error."),
   DEFAULT_CASE_NO_DEFAULT(Severity.WARNING, "default case should at least have one child node with default value."),
   DEFAULT_EXCEED(Severity.WARNING, "the size of default statement exceed the max elements."),
   DERIVEDTYPE_EXPAND_VALUESPACE(Severity.WARNING, "derived type can not expand value space."),
   DEVIATE_NOT_SUPPORT_ONLY_ONE("deviation MUST only have one not-supported deviate."),
   DEVIATE_NOT_ALLOWED("the property:${property} is not allowed for deviate:${deviate}"),
   DUPLICATE_DEFINITION("duplicate definition occur."),
   ENUM_NO_VALUE(Severity.WARNING, "enum's value MUST be specified for current highest value reaching max(2147483647)"),
   EXTENSION_REGISTER_ERROR("extension statement register has error. "),
   FRACTIONDIGITS_ERROR("fraction-digits MUST be integer between 1 and 18."),
   IDENTITYREF_CANNOT_RESTRICTED(Severity.WARNING, "derived identityRef type can not be restricted."),
   IMPORT_MODULE_NOT_VALIDATE("the import module:${module} is not validated."),
   INCLUDE_MODULE_NOT_VALIDATE("the include module:${module} is not validated."),
   INCLUDE_MODULE_NOT_BELONGSTO_SELF("included submodule is not belongs to self module."),
   INCOMPATIBLE_YANG_VERSION(Severity.WARNING, "incompatible yang version."),
   INVALID_ARG("invalid argument occurs."),
   INVALID_BIT_POSTION("invalid bit position occurs. It MUST be an unsigned int"),
   INVALID_CHARACTER("contains invalid character."),
   INVALID_DEFAULTVALUE("invalid default value."),
   INVALID_ENUM_VALUE("invalid enum value occurs. It MUST be an integer."),
   INVALID_IDENTIFIER("invalid identifier,alphabet or digital or \"_\" or \"-\" or \".\" is expected, and should start with alphabet or \"_\", and should not start with \"xml\"."),
   INVALID_IDENTIFIER_REF("invalid identifier ref, it should be [prefix:]identifier format."),
   INVALID_PREFIX("invalid prefix:${name} occurs."),
   INVALID_PATTERN("invalid pattern:${name} occurs"),
   INVALID_REVISION_FORMAT("revision date must be a string with 'yyyy-mm-dd' format."),
   INVALID_SCHEMAPATH("invalid schema path occurs."),
   INVALID_SUBSTATEMENT("invalid sub-statement occurs."),
   INVALID_TYPEDEF_NAME("The name of typedef is invalid, it should be an identifier except builtin-type."),
   INVALID_VALUE("invalid value."),
   INVALID_VERSION("invalid yang version."),
   INVALID_XPATH("invalid xpath expression."),
   INVALID_XPATH_LEAFREF_PREDICATE_MUST_KEY(Severity.WARNING, "invalid xpath:${xpath}. the xpath's predicate of leafref path must only contains key node, but this node:{nodename} is not key node."),
   INVALID_XPATH_LEAFREF_POINT_LEAF("invalid xpath:${xpath}. leafref path must point to leaf node, but this node:{nodename} is not leaf node."),
   INVALID_XPATH_LEAFREF_PREDICATE_MUST_EQUALITY(Severity.WARNING, "invalid xpath:${xpath}. leafref path's predicates must be equality expression"),
   INVALID_XPATH_LEAFREF_PREDICATE_ONLY_LIST(Severity.WARNING, "invalid xpath:${xpath}. leafref path's predicate:${predicate} must be only exist in key nodes. But the node:${nodename} is not the key node."),
   INVALID_XPATH_TERMIANL_HAS_CHILD("invalid xpath:${xpath}. the node:${nodename}(${keyword}) has no capability to own a child."),
   INVALID_XPATH_UNCMPATIBLE_CHILD(Severity.WARNING, "invalid xpath:${xpath}. the configuration attribute of node:${nodename} is incompatible with context node:${context}'."),
   INVALID_XPATH_UNRECOGNIZED_CHILD("invalid xpath:${xpath}. the node:${nodename} has no child node:${child}."),
   INVALID_XPATH_INACTIVE_CHILD(Severity.WARNING, "invalid xpath:${xpath}. the node:${nodename} has inactive child node:${child}."),
   INVALID_XPATH_WHEN_ACCESS_CHILD(Severity.WARNING, "invalid xpath:${xpath}. xpath for when MUST NOT access the children of context node."),
   KEY_CONFIG_ATTRIBUTE_DIFF_WITH_LIST("the key node's config attribute MUST be the same with list's config attribute."),
   KEY_NODE_NOT_FOUND("can not find the leaf node:${name} defined in key statement."),
   KEY_NODE_SHOULD_NOT_EMPTY_TYPE(Severity.ERROR,"the type of key node should not be empty type."),
   KEY_NODE_INACTIVE("the leaf node:${name} defined in key statement is inactive,it may be un-supported by deviation or if-features are evaluated to be false."),
   LEAFREF_CANNOT_RESTRICTED_BY_PATH(Severity.WARNING, "derived leafref type can not be restricted by path statement."),
   LEAFREF_SHOULD_NO_DEFAULT(Severity.WARNING, "The data node with leafref type should not define default value."),
   LIST_NO_KEY("config true list MUST have key.  "),
   MANDATORY_CAN_NOT_DELETED("mandatory statement:${name} is not allowed to be deleted"),
   MANDATORY_HASDEFAULT(Severity.WARNING, "mandatory node must have no default value."),
   MANDATORY_MISSING("mandatory statement:${name} missing."),
   MISSING_CASE("the case named:${name} is not found."),
   MISSING_DEPENDENCE_MODULE("The dependent module:${name} is not found."),
   MISSING_GROUPING("the grouping named:${name} is not found."),
   MISSING_TARGET("the target node is not found."),
   MISSING_CLASS_REG("can not find the parser policy for statement:${keyword}. please check whether register parser policy for this statement."),
   NOTIFICATION_NOT_IN_DATATREE("A notification MUST NOT be defined within an rpc, action, or another notification"),
   NOT_SUPPORT_CONFIG(" the target statement doesn't support config attribute."),
   NOT_SUPPORT_IFFEATURE("the target statement doesn't support if-feature attribute."),
   NOT_SUPPORT_MUST("the target statement doesn't support must attribute."),
   PROPERTY_EXIST("property:${name} exists."),
   PROPERTY_NOT_EXIST("property:${name} not exists."),
   PROPERTY_NOT_MATCH("property:${name} not match."),
   RANGE_OR_LENGTH_NOT_SUBSET_OF_DERIVED("range or length is not subset of derived type or builtin-type's range."),
   REFERENCE_NODE_NOT_FOUND("can not find the reference node "),
   SECTIONS_MUST_ASCEND_ORDER("sections in range or length MUST be ascend order"),
   TARGET_CAN_NOT_AUGMENTED("the target node can not be augmented."),
   TOO_MANY_DEPENDECE_MODULES("Too many dependence modules are found."),
   UNEXPECTED_IDENTIFIER("unexpected identifier occurs."),
   UNION_CANNOT_RESTRICTED("union can not be restricted."),
   UNIQUE_NODE_CONFIG_ATTRI_DIFF("all of the unique nodes' config attribute MUST be the same."),
   UNIQUE_NODE_NOT_FOUND("can not find the leaf node:${name} defined in unique statement."),
   UNIQUE_NODE_INACTIVE("the leaf node:${name} defined in unique statement is inactive,it may be un-supported by deviation or if-features are evaluated to be false."),
   UNKNOWN_EXTENSION("unrecognized extension name."),
   UNRECOGNIZED_FEATURE("unrecognized feature:${name} occurs."),
   UNRECOGNIZED_IDENTITY("unrecognized identity:${name} occurs."),
   UNRECOGNIZED_KEYWORD("unrecognized yang keyword occurs."),
   UNRECOGNIZED_TYPE("unrecognized type occurs. please check whether the name or prefix is correct."),
   UNUSED_IMPORT("the imported module:${name} not used."),
   UNUSED_TYPEDEF("the typedef:${name} not used."),
   UNUSED_GROUPING("the grouping:${name} not used."),
   UNVERIFIED("this statement hasn't been verified."),
   WRONG_PATH("the path points to incompatible node or unrecognized node."),
   WRONG_TYPE_DEPENDECE_MODULE("The type of dependence module is wrong."),
   WRONG_USING_EXTENSION("using extension error, it should be {prefix}:{extension} style."),
   MISSING_PREDICATES(Severity.WARNING, "The xpath may point to a node set, please add suitable predicts after listNode ${listNode}. xpath:${xpath}."),
   PREDICATES_MUST_EXPRESSION(Severity.WARNING, "invalid xpath:${xpath}. The predicate containing the current function should be an expression.");

   private String fieldName;
   private Severity severity;
   private String reference;

   private ErrorCode(String name) {
      this.severity = Severity.ERROR;
      this.fieldName = name;
   }

   private ErrorCode(Severity severity, String name) {
      this.severity = Severity.ERROR;
      this.severity = severity;
      this.fieldName = name;
   }

   private ErrorCode(String name, String reference) {
      this(Severity.ERROR, name, reference);
   }

   private ErrorCode(Severity severity, String name, String reference) {
      this.severity = Severity.ERROR;
      this.severity = severity;
      this.fieldName = name;
      this.reference = reference;
   }

   public String getFieldName() {
      return this.fieldName;
   }

   private String formatFieldName(String[] args) {
      if (args != null && args.length != 0) {
         String output = this.fieldName;
         int length = args.length;

         for(int i = 0; i < length; ++i) {
            String arg = args[i];
            String[] argInfo = arg.split("=", 2);
            if (argInfo.length != 2) {
               throw new IllegalArgumentException("wrong format");
            }

            String para = argInfo[0];
            String value = argInfo[1];
            output = output.replace("${" + para + "}", value);
         }

         return output;
      } else {
         return this.fieldName;
      }
   }

   public String toString(String[] args) {
      StringBuffer sb = new StringBuffer();
      sb.append(this.formatFieldName(args));
      if (null != this.reference) {
         sb.append(" reference:");
         sb.append(this.reference);
      }

      return sb.toString();
   }

   public Severity getSeverity() {
      return this.severity;
   }
}
