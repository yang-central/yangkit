package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;
import org.yangcentral.yangkit.model.impl.stmt.*;
import org.yangcentral.yangkit.register.YangParentStatementInfo;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class YangDataStructureImpl extends SchemaNodeImpl implements YangStructure {
    private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
    private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
    private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
    private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
    private MustSupportImpl mustSupport = new MustSupportImpl();

    public static void register(){
        YangUnknownParserPolicy unknownParserPolicy = new YangUnknownParserPolicy(YANG_KEYWORD, YangDataStructureImpl.class,
                Arrays.asList(BuildPhase.GRAMMAR,BuildPhase.SCHEMA_BUILD));
        YangStatementDef yangStatementDef = new YangStatementDef(YANG_KEYWORD,"name",true);
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.MUST.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.STATUS.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.DESCRIPTION.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.REFERENCE.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.TYPEDEF.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.GROUPING.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.CONTAINER.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.LEAF.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.LEAFLIST.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.CHOICE.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.ANYDATA.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.ANYXML.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.USES.getQName(),new Cardinality()));
        unknownParserPolicy.setStatementDef(yangStatementDef);
        YangParentStatementInfo moduleParentInfo = new YangParentStatementInfo(YangBuiltinKeyword.MODULE.getQName(),
                new Cardinality(),YangDataStructureChecker.class);
        YangParentStatementInfo submoduleParentInfo = new YangParentStatementInfo(YangBuiltinKeyword.SUBMODULE.getQName(),
                new Cardinality(),YangDataStructureChecker.class);
        unknownParserPolicy.addParentStatementInfo(moduleParentInfo);
        unknownParserPolicy.addParentStatementInfo(submoduleParentInfo);

        YangUnknownRegister.getInstance().register(unknownParserPolicy);
    }

    public YangDataStructureImpl(String argStr) {
        super(argStr);
    }
    public void setContext(YangContext context) {
        super.setContext(context);
        this.typedefContainer.setYangContext(context);
        this.groupingDefContainer.setYangContext(context);
        this.schemaNodeContainer.setYangContext(context);
        this.dataDefContainer.setYangContext(context);
    }

    @Override
    public List<DataDefinition> getDataDefChildren() {
        return dataDefContainer.getDataDefChildren();
    }

    @Override
    public DataDefinition getDataDefChild(String name) {
        return dataDefContainer.getDataDefChild(name);
    }

    @Override
    public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
        return dataDefContainer.addDataDefChild(dataDefinition);
    }

    @Override
    public List<Grouping> getGroupings() {
        return groupingDefContainer.getGroupings();
    }

    @Override
    public Grouping getGrouping(String name) {
        return groupingDefContainer.getGrouping(name);
    }

    @Override
    public Must getMust(int index) {
        return mustSupport.getMust(index);
    }

    @Override
    public Must getMust(String condition) {
        return mustSupport.getMust(condition);
    }

    @Override
    public List<Must> getMusts() {
        return mustSupport.getMusts();
    }

    @Override
    public void setMusts(List<Must> musts) {
        mustSupport.setMusts(musts);
    }

    @Override
    public ValidatorResult addMust(Must must) {
        return mustSupport.addMust(must);
    }

    @Override
    public void removeMust(String condition) {
        mustSupport.removeMust(condition);
    }

    @Override
    public ValidatorResult updateMust(Must must) {
        return mustSupport.updateMust(must);
    }

    @Override
    public ValidatorResult validateMusts() {
        return mustSupport.validateMusts();
    }

    @Override
    public List<SchemaNode> getSchemaNodeChildren() {
        return schemaNodeContainer.getSchemaNodeChildren();
    }

    @Override
    public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
        return schemaNodeContainer.addSchemaNodeChild(schemaNode);
    }

    @Override
    public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
        return schemaNodeContainer.addSchemaNodeChildren(schemaNodes);
    }

    @Override
    public SchemaNode getSchemaNodeChild(QName identifier) {
        return schemaNodeContainer.getSchemaNodeChild(identifier);
    }

    @Override
    public DataNode getDataNodeChild(QName identifier) {
        return schemaNodeContainer.getDataNodeChild(identifier);
    }

    @Override
    public List<DataNode> getDataNodeChildren() {
        return schemaNodeContainer.getDataNodeChildren();
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
    public void removeSchemaNodeChild(QName identifier) {
        schemaNodeContainer.removeSchemaNodeChild(identifier);
    }

    @Override
    public void removeSchemaNodeChild(SchemaNode schemaNode) {
        schemaNodeContainer.removeSchemaNodeChild(schemaNode);
    }

    @Override
    public SchemaNode getMandatoryDescendant() {
        return schemaNodeContainer.getMandatoryDescendant();
    }

    @Override
    public List<Typedef> getTypedefs() {
        return typedefContainer.getTypedefs();
    }

    @Override
    public Typedef getTypedef(int index) {
        return typedefContainer.getTypedef(index);
    }

    @Override
    public Typedef getTypedef(String name) {
        return typedefContainer.getTypedef(name);
    }

    @Override
    public String getKeyword() {
        return null;
    }

    @Override
    public Extension getExtension() {
        return null;
    }

    @Override
    public void setExtension(Extension extension) {

    }

    @Override
    public boolean checkChild(YangStatement subStatement) {
        boolean result =  super.checkChild(subStatement);
        if(!result){
            return false;
        }
        YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
        switch (builtinKeyword){
            case CONTAINER:
            case LIST:
            case LEAF:
            case LEAFLIST:
            case ANYDATA:
            case ANYXML:
            case CHOICE:
            {
                if(getContext().getSchemaNodeIdentifierCache().containsKey(subStatement.getArgStr())){
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
            case GROUPING:{
                if(getGrouping(subStatement.getArgStr()) != null){
                    return false;
                }
                return true;
            }
            case MUST:{
                if(getMust(subStatement.getArgStr()) != null){
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
        //clear current state
        this.typedefContainer.removeTypedefs();
        this.groupingDefContainer.removeGroupings();
        this.dataDefContainer.removeDataDefs();
        this.schemaNodeContainer.removeSchemaNodeChildren();
        this.mustSupport.removeMusts();
        super.clearSelf();
    }

    protected ValidatorResult initSelf() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        validatorResultBuilder.merge(super.initSelf());

        List<YangElement> subElements = this.getSubElements();

        for (YangElement subElement : subElements) {
            if (subElement instanceof YangBuiltinStatement) {
                YangBuiltinStatement builtinStatement = (YangBuiltinStatement) subElement;
                YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
                switch (builtinKeyword) {
                    case CONTAINER:
                    case LIST:
                    case LEAF:
                    case LEAFLIST:
                    case ANYDATA:
                    case ANYXML:
                    case CHOICE:
                    case USES:
                        DataDefinition newDataDefinition = (DataDefinition) builtinStatement;
                        validatorResultBuilder.merge(this.dataDefContainer.addDataDefChild(newDataDefinition));
                        break;
                    case TYPEDEF:
                        Typedef newTypedef = (Typedef) builtinStatement;
                        validatorResultBuilder.merge(this.typedefContainer.addTypedef(newTypedef));
                        break;
                    case GROUPING:
                        Grouping newGrouping = (Grouping) builtinStatement;
                        validatorResultBuilder.merge(this.groupingDefContainer.addGrouping(newGrouping));
                        break;
                    case MUST: {
                        Must must = (Must) builtinStatement;
                        validatorResultBuilder.merge(addMust(must));
                        break;
                    }
                }
            }
        }

        return validatorResultBuilder.build();
    }

    protected ValidatorResult buildSelf(BuildPhase phase) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        validatorResultBuilder.merge(super.buildSelf(phase));
        switch (phase) {
            case SCHEMA_BUILD:

                for (DataDefinition dataDefinition : this.getDataDefChildren()) {
                    validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
                }

            default:
                return validatorResultBuilder.build();
        }
    }
    @Override
    public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
        return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
    }

    public List<YangStatement> getEffectiveSubStatements() {
        List<YangStatement> statements = new ArrayList<>();
        statements.addAll(getEffectiveSchemaNodeChildren());
        statements.addAll(this.groupingDefContainer.getGroupings());
        statements.addAll(this.mustSupport.getMusts());
        statements.addAll(this.typedefContainer.getTypedefs());
        statements.addAll(super.getEffectiveSubStatements());
        return statements;
    }

    @Override
    public boolean isConfig() {
        return false;
    }

    @Override
    public QName getIdentifier() {
        return new QName(this.getContext().getNamespace(),this.getArgStr());
    }
}
