package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;
import org.yangcentral.yangkit.model.impl.stmt.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class YangDataStructureImpl extends SchemaNodeImpl implements YangDataStructure {
    private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
    private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
    private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
    private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
    private MustSupportImpl mustSupport = new MustSupportImpl();

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
                    case TYPEDEF:
                        Typedef newTypedef = (Typedef)builtinStatement;
                        validatorResultBuilder.merge(this.typedefContainer.addTypedef(newTypedef));
                        break;
                    case GROUPING:
                        Grouping newGrouping = (Grouping)builtinStatement;
                        validatorResultBuilder.merge(this.groupingDefContainer.addGrouping(newGrouping));
                        break;
                    case MUST:{
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
                Iterator iterator = this.getDataDefChildren().iterator();

                while(iterator.hasNext()) {
                    DataDefinition dataDefinition = (DataDefinition)iterator.next();
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
        List<YangStatement> statements = new ArrayList();
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
