package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangData;
import org.yangcentral.yangkit.model.impl.stmt.DataDefContainerImpl;
import org.yangcentral.yangkit.model.impl.stmt.SchemaNodeContainerImpl;
import org.yangcentral.yangkit.model.impl.stmt.YangStatementImpl;
import org.yangcentral.yangkit.register.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class YangDataImpl extends YangStatementImpl implements YangData  {
    private String keyword;
    private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
    private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
    private final static QName YANG_KEYWORD = new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","yang-data");
    static {
        YangStatementRegister.getInstance().register(YANG_KEYWORD,
                new YangStatementParserPolicy(YANG_KEYWORD, YangDataImpl.class,
                        Arrays.asList(BuildPhase.SCHEMA_BUILD)));
        YangUnknownParserPolicy unknownParserPolicy = new YangUnknownParserPolicy(YANG_KEYWORD, YangDataImpl.class,
                Arrays.asList(BuildPhase.SCHEMA_BUILD));
        YangStatementDef yangStatementDef = new YangStatementDef(YANG_KEYWORD,"name",true);
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.CONTAINER.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.LEAF.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.LEAFLIST.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.CHOICE.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.ANYDATA.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.ANYXML.getQName(),new Cardinality());
        yangStatementDef.addSubStatementInfo(YangBuiltinKeyword.USES.getQName(),new Cardinality());
        unknownParserPolicy.setStatementDef(yangStatementDef);
        unknownParserPolicy.addParentStatementInfo(new YangParentStatementInfo(YangBuiltinKeyword.MODULE.getQName(),new Cardinality()));
        unknownParserPolicy.addParentStatementInfo(new YangParentStatementInfo(YangBuiltinKeyword.SUBMODULE.getQName(),new Cardinality()));

        YangUnknownRegister.getInstance().register(unknownParserPolicy);
    }
    public YangDataImpl(String keyword,String argStr) {
        super(argStr);
        this.keyword = keyword;
    }

    @Override
    public void setContext(YangContext context) {
        super.setContext(context);
        dataDefContainer.setYangContext(this.getContext());
        schemaNodeContainer.setYangContext(this.getContext());
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
    public QName getYangKeyword() {
        return YANG_KEYWORD;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public Extension getExtension() {
        return null;
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
            default:{
                return true;
            }
        }
    }

    @Override
    protected void clearSelf() {
        //clear current state
        this.dataDefContainer.removeDataDefs();
        this.schemaNodeContainer.removeSchemaNodeChildren();
        super.clearSelf();
    }

    protected ValidatorResult initSelf() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        validatorResultBuilder.merge(super.initSelf());

        List<YangElement> subElements = this.getSubElements();
        Iterator iterator = subElements.iterator();

        while(iterator.hasNext()) {
            YangElement subElement = (YangElement)iterator.next();
            if (subElement instanceof YangBuiltinStatement) {
                YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
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
                        DataDefinition newDataDefinition = (DataDefinition)builtinStatement;
                        validatorResultBuilder.merge(this.dataDefContainer.addDataDefChild(newDataDefinition));
                        break;
                }
            }
        }

        return validatorResultBuilder.build();
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

    public void removeSchemaNodeChild(QName identifier) {
        this.schemaNodeContainer.removeSchemaNodeChild(identifier);
    }

    public void removeSchemaNodeChild(SchemaNode schemaNode) {
        this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
    }

    public SchemaNode getMandatoryDescendant() {
        return this.schemaNodeContainer.getMandatoryDescendant();
    }

    protected ValidatorResult buildSelf(BuildPhase phase) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        validatorResultBuilder.merge(super.buildSelf(phase));
        switch (phase) {
            case SCHEMA_BUILD:
                Iterator iterator = this.getDataDefChildren().iterator();

                while(iterator.hasNext()) {
                    DataDefinition dataDefinition = (DataDefinition)iterator.next();
                    validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
                }

            default:
                return validatorResultBuilder.build();
        }
    }

    public List<YangStatement> getEffectiveSubStatements() {
        List<YangStatement> statements = new ArrayList();
        statements.addAll(this.dataDefContainer.getDataDefChildren());
        statements.addAll(super.getEffectiveSubStatements());
        return statements;
    }
}
