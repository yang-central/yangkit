package org.yangcentral.yangkit.model.api.stmt;

public interface Action extends Identifiable, IfFeatureSupport, YangBuiltinStatement,SchemaNode, SchemaNodeContainer, TypedefContainer, GroupingDefContainer {
   Input getInput();

   void setInput(Input input);

   Output getOutput();

   void setOutput(Output output);
}
