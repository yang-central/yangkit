package org.yangcentral.yangkit.model.api.stmt;

public interface Notification extends IfFeatureSupport, SchemaNode, SchemaNodeContainer, DataDefContainer,
        TypedefContainer, GroupingDefContainer, MustSupport, Identifiable, Augmentable,YangBuiltinStatement,TreeNode {
   default boolean isSchemaTreeRoot() {
      return true;
   }
}
