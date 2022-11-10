package org.yangcentral.yangkit.model.api.stmt;

/**
 * 功能描述
 *
 * @author llly

 * @since 2022-11-09
 */
public interface Operation extends Identifiable, IfFeatureSupport, YangBuiltinStatement,SchemaNode,
        SchemaNodeContainer, TypedefContainer, GroupingDefContainer{
    Input getInput();

    void setInput(Input input);

    Output getOutput();

    void setOutput(Output output);
}
