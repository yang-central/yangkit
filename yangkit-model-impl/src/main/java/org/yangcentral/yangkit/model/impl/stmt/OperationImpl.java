package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 功能描述
 *
 * @author llly
 * @since 2022-11-09
 */
public abstract class OperationImpl extends SchemaNodeImpl implements Operation {
    private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
    private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
    private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
    private Input input;
    private Output output;
    private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();
    private QName identifier;

    public OperationImpl(String argStr) {
        super(argStr);
    }

    public void setContext(YangContext context) {
        super.setContext(context);
        this.groupingDefContainer.setYangContext(context);
        this.schemaNodeContainer.setYangContext(context);
        this.typedefContainer.setYangContext(context);
    }

    public List<Grouping> getGroupings() {
        return this.groupingDefContainer.getGroupings();
    }

    public Grouping getGrouping(String name) {
        return this.groupingDefContainer.getGrouping(name);
    }

    public Input getInput() {
        return this.input;
    }

    public Output getOutput() {
        return this.output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public List<SchemaNode> getSchemaNodeChildren() {
        return this.schemaNodeContainer.getSchemaNodeChildren();
    }

    public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
        return this.schemaNodeContainer.addSchemaNodeChild(schemaNode);
    }

    public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
        return this.schemaNodeContainer.addSchemaNodeChildren(schemaNodes);
    }

    public SchemaNode getSchemaNodeChild(QName identifier) {
        return this.schemaNodeContainer.getSchemaNodeChild(identifier);
    }

    public DataNode getDataNodeChild(QName identifier) {
        return this.schemaNodeContainer.getDataNodeChild(identifier);
    }

    public List<DataNode> getDataNodeChildren() {
        return this.schemaNodeContainer.getDataNodeChildren();
    }

    @Override
    public List<SchemaNode> getTreeNodeChildren() {
        return schemaNodeContainer.getTreeNodeChildren();
    }

    @Override
    public SchemaNode getTreeNodeChild(QName identifier) {
        return schemaNodeContainer.getTreeNodeChild(identifier);
    }

    @Override
    public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
        return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
    }

    public void removeSchemaNodeChild(QName identifier) {
        this.schemaNodeContainer.removeSchemaNodeChild(identifier);
    }

    public void removeSchemaNodeChild(SchemaNode schemaNode) {
        this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
    }

    public SchemaNode getMandatoryDescendant() {
        return null;
    }

    public List<Typedef> getTypedefs() {
        return this.typedefContainer.getTypedefs();
    }

    public Typedef getTypedef(int index) {
        return this.typedefContainer.getTypedef(index);
    }

    public Typedef getTypedef(String defName) {
        return this.typedefContainer.getTypedef(defName);
    }


    public List<IfFeature> getIfFeatures() {
        return this.ifFeatureSupport.getIfFeatures();
    }

    public ValidatorResult addIfFeature(IfFeature ifFeature) {
        return this.ifFeatureSupport.addIfFeature(ifFeature);
    }

    @Override
    public IfFeature getIfFeature(String exp) {
        return ifFeatureSupport.getIfFeature(exp);
    }

    @Override
    public IfFeature removeIfFeature(String exp) {
        return ifFeatureSupport.removeIfFeature(exp);
    }

    public void setIfFeatures(List<IfFeature> ifFeatures) {
        this.ifFeatureSupport.setIfFeatures(ifFeatures);
    }

    public boolean evaluateFeatures() {
        return this.ifFeatureSupport.evaluateFeatures();
    }

    @Override
    public boolean checkChild(YangStatement subStatement) {
        boolean result =  super.checkChild(subStatement);
        if(!result){
            return false;
        }
        YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
        switch (builtinKeyword){
            case GROUPING:{
                if(getGrouping(subStatement.getArgStr()) != null){
                    return false;
                }
                return true;
            }
            case TYPEDEF:{
                if(getTypedef(subStatement.getArgStr()) != null){
                    return false;
                }
                return true;
            }
            case IFFEATURE:{
                if(getSubStatement(builtinKeyword.getQName(),subStatement.getArgStr()) != null){
                    return false;
                }
                return true;
            }
            default:{
                return true;
            }
        }
    }

    @Override
    protected void clearSelf() {
        groupingDefContainer.removeGroupings();
        typedefContainer.removeTypedefs();
        ifFeatureSupport.removeIfFeatures();
        this.schemaNodeContainer.removeSchemaNodeChildren();
        this.input = null;
        this.output = null;
        super.clearSelf();
    }

    protected ValidatorResult initSelf() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

        List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.GROUPING.getQName());
        if (matched.size() > 0) {
            for (YangStatement statement : matched) {
                Grouping grouping = (Grouping)statement;
                validatorResultBuilder.merge(this.groupingDefContainer.addGrouping(grouping));
            }
        }

        matched = this.getSubStatement(YangBuiltinKeyword.TYPEDEF.getQName());
        if (matched.size() > 0) {
            for (YangStatement statement : matched) {
                Typedef typedef = (Typedef)statement;
                validatorResultBuilder.merge(this.typedefContainer.addTypedef(typedef));
            }
        }

        matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());
        if (matched.size() > 0) {
            for (YangStatement statement : matched) {
                IfFeature ifFeature = (IfFeature)statement;
                validatorResultBuilder.merge(this.ifFeatureSupport.addIfFeature(ifFeature));
            }
        }

        matched = this.getSubStatement(YangBuiltinKeyword.INPUT.getQName());
        if (matched.size() > 0) {
            this.input = (Input)matched.get(0);
        }

        matched = this.getSubStatement(YangBuiltinKeyword.OUTPUT.getQName());
        if (matched.size() > 0) {
            this.output = (Output)matched.get(0);
        }

        return validatorResultBuilder.build();
    }



    protected ValidatorResult buildSelf(BuildPhase phase) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
        switch (phase) {
            case SCHEMA_BUILD:
                //this.setSchemaTreeType(SchemaTreeType.RPCTREE);
                if (this.input != null) {
                    this.schemaNodeContainer.addSchemaNodeChild(this.input);
                } else {
                    Input input = new InputImpl(null);
                    input.setContext(new YangContext(this.getContext()));
                    input.setElementPosition(this.getElementPosition());
                    input.setParentStatement(this);
                    input.init();
                    input.build();
                    this.schemaNodeContainer.addSchemaNodeChild(input);
                }

                if (this.output != null) {
                    this.schemaNodeContainer.addSchemaNodeChild(this.output);
                } else {
                    Output output = new OutputImpl(null);
                    output.setContext(new YangContext(this.getContext()));
                    output.setElementPosition(this.getElementPosition());
                    output.setParentStatement(this);
                    output.init();
                    output.build();
                    this.schemaNodeContainer.addSchemaNodeChild(output);
                }
            default:
                return validatorResultBuilder.build();
        }
    }

    public boolean isConfig() {
        return false;
    }

    public QName getIdentifier() {
        if (this.identifier != null) {
            return this.identifier;
        } else {
            this.identifier = new QName(this.getContext().getNamespace(), this.getArgStr());
            return this.identifier;
        }
    }

    public List<YangStatement> getEffectiveSubStatements() {
        List<YangStatement> statements = new ArrayList<>();
        statements.addAll(this.groupingDefContainer.getGroupings());
        statements.addAll(this.typedefContainer.getTypedefs());
        if (this.input != null && this.input.isActive()) {
            statements.add(this.input);
        }

        if (this.output != null && this.output.isActive()) {
            statements.add(this.output);
        }

        statements.addAll(this.ifFeatureSupport.getIfFeatures());
        statements.addAll(super.getEffectiveSubStatements());
        return statements;
    }
}
