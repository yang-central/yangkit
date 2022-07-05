package org.yangcentral.yangkit.model.api.stmt;

public interface Rpc extends IfFeatureSupport, SchemaNode, SchemaNodeContainer, TypedefContainer, GroupingDefContainer, Identifiable {
   Input getInput();

   Output getOutput();

   void setOutput(Output var1);

   void setInput(Input var1);

   default boolean isSchemaTreeRoot() {
      return true;
   }
}
