package org.yangcentral.yangkit.model.api.stmt;

public interface Action extends Identifiable, IfFeatureSupport, SchemaNode, SchemaNodeContainer, TypedefContainer, GroupingDefContainer {
   Input getInput();

   void setInput(Input var1);

   Output getOutput();

   void setOutput(Output var1);
}
