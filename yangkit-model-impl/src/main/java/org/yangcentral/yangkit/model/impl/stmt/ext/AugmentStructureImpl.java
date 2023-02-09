package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.AugmentStructure;
import org.yangcentral.yangkit.model.impl.schema.SchemaPathImpl;
import org.yangcentral.yangkit.model.impl.stmt.*;
import org.yangcentral.yangkit.register.YangParentStatementInfo;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.*;

public class AugmentStructureImpl extends SchemaNodeImpl implements AugmentStructure {
    private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
    private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
    private SchemaPath targetPath;
    private SchemaNode target;
    private Extension extension;
    public static void register(){
        YangUnknownParserPolicy unknownParserPolicy = new YangUnknownParserPolicy(YANG_KEYWORD, AugmentStructureImpl.class,
                Arrays.asList(BuildPhase.GRAMMAR,BuildPhase.SCHEMA_BUILD,BuildPhase.SCHEMA_EXPAND,BuildPhase.SCHEMA_TREE));
        YangStatementDef yangStatementDef = new YangStatementDef(YANG_KEYWORD,"path",true);
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.STATUS.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.DESCRIPTION.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.REFERENCE.getQName(),new Cardinality(0,1)));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.CONTAINER.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.LEAF.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.LEAFLIST.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.CHOICE.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.ANYDATA.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.ANYXML.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.USES.getQName(),new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.CASE.getQName(),new Cardinality()));
        unknownParserPolicy.setStatementDef(yangStatementDef);
        YangParentStatementInfo moduleParentInfo = new YangParentStatementInfo(YangBuiltinKeyword.MODULE.getQName(),
                new Cardinality());
        YangParentStatementInfo submoduleParentInfo = new YangParentStatementInfo(YangBuiltinKeyword.SUBMODULE.getQName(),
                new Cardinality());
        unknownParserPolicy.addParentStatementInfo(moduleParentInfo);
        unknownParserPolicy.addParentStatementInfo(submoduleParentInfo);

        YangUnknownRegister.getInstance().register(unknownParserPolicy);
    }
    public AugmentStructureImpl(String argStr) {
        super(argStr);
    }
    public void setContext(YangContext context) {
        super.setContext(context);
        this.dataDefContainer.setYangContext(context);
        this.schemaNodeContainer.setYangContext(context);
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
    public SchemaNode getTarget() {
        return target;
    }

    @Override
    public void setTarget(SchemaNode target) {
        this.target = target;
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public void setTargetPath(SchemaPath schemaPath) {
        this.targetPath = schemaPath;
    }



    @Override
    public boolean isConfig() {
        return false;
    }


    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public boolean hasDefault() {
        return false;
    }

    @Override
    public SchemaTreeType getSchemaTreeType() {
        return SchemaTreeType.YANGDATATREE;
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
    public String getKeyword() {
        return null;
    }

    @Override
    public Extension getExtension() {
        return extension;
    }

    @Override
    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    @Override
    public boolean checkChild(YangStatement subStatement) {
        boolean result = super.checkChild(subStatement);
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
            case CASE: {
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
        dataDefContainer.removeDataDefs();
        this.schemaNodeContainer.removeSchemaNodeChildren();
        super.clearSelf();
    }

    protected ValidatorResult initSelf() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
        List<YangElement> subElements = this.getSubElements();
        Iterator elementIterator = subElements.iterator();

        while(elementIterator.hasNext()) {
            YangElement subElement = (YangElement)elementIterator.next();
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
                    case CASE:
                    case USES:
                        DataDefinition newDataDefinition = (DataDefinition)builtinStatement;
                        validatorResultBuilder.merge(this.addDataDefChild(newDataDefinition));
                        break;
                }
            }
        }

        return validatorResultBuilder.build();
    }

    protected ValidatorResult buildSelf(BuildPhase phase) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
        Iterator iterator;
        SchemaNode child;
        switch (phase) {
            case GRAMMAR:{

                break;
            }
            case SCHEMA_BUILD:{
                List<DataDefinition> dataDefChildren = this.getDataDefChildren();
                Iterator schemaChildrenIt = dataDefChildren.iterator();

                while(schemaChildrenIt.hasNext()) {
                    DataDefinition dataDefinition = (DataDefinition)schemaChildrenIt.next();
                    validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
                }
                break;
            }
            case SCHEMA_EXPAND:{
                try {
                    SchemaPath targetPath = SchemaPathImpl.from(getContext().getCurModule(),this,this.getArgStr());
                    if (targetPath instanceof SchemaPath.Descendant) {
                        validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                                ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
                    } else {
                        this.setTargetPath(targetPath);
                        SchemaNode target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
                        if (target == null) {
                            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                                    ErrorCode.MISSING_TARGET.getFieldName()));
                            break;
                        }

                        if (!(target instanceof Augmentable) &&(target.getSchemaTreeType() != SchemaTreeType.YANGDATATREE)) {
                            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                                    ErrorCode.TARGET_CAN_NOT_AUGMENTED.getFieldName()));
                        }
                        this.setTarget(target);
                        iterator = this.getSchemaNodeChildren().iterator();
                        while(iterator.hasNext()) {
                            child = (SchemaNode)iterator.next();
                            if (child instanceof DataDefinition) {
                                if (!(this.target instanceof DataDefContainer)) {
                                    validatorResultBuilder.addRecord(
                                            ModelUtil.reportError(child, ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                                } else if (this.target instanceof Choice && child instanceof Uses) {
                                    validatorResultBuilder.addRecord(
                                            ModelUtil.reportError(child,ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                                } else if (this.target instanceof Choice) {
                                    ChoiceImpl choice = (ChoiceImpl)this.target;
                                    Case ch = null;
                                    if (child instanceof Case) {
                                        ch = (Case)child;
                                    } else {
                                        ch = new CaseImpl(child.getArgStr());
                                        ch.setContext(new YangContext(this.getContext()));
                                        ch.setShortCase(true);
                                        ch.addDataDefChild((DataDefinition)child);
                                        ch.addSchemaNodeChild(child);
                                        this.removeSchemaNodeChild(child);
                                        this.removeSchemaNodeChild(ch);//remove the old if it's built
                                        this.addSchemaNodeChild(ch);
                                        ch.init();
                                        ch.build();
                                    }
                                    choice.removeCase(ch.getIdentifier());//remove the old if it's built
                                    if (!choice.addCase(ch)) {
                                        validatorResultBuilder.addRecord(
                                                ModelUtil.reportError(child,ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                                    }
                                }
                            }
                        }
                        SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) this.getTarget();
                        schemaNodeContainer.addSchemaNodeChild(this);
                    }
                } catch (ModelException e) {
                    validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                            e.getSeverity(),ErrorTag.BAD_ELEMENT,e.getDescription()));
                }

                break;
            }

            case SCHEMA_TREE:{
                iterator = this.getSchemaNodeChildren().iterator();

                while(iterator.hasNext()) {
                    child = (SchemaNode)iterator.next();
                    if (child instanceof Case && ((Case)child).isShortCase()) {
                        validatorResultBuilder.merge(child.build(phase));
                    }
                }
                break;
            }

        }

        return validatorResultBuilder.build();
    }
    public QName getIdentifier() {
        return new QName(this.getContext().getNamespace(), this.getArgStr() + this.hashCode());
    }

    public SchemaPath.Absolute getSchemaPath() {
        throw new IllegalArgumentException("un-support");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AugmentImpl)) {
            return false;
        } else {
            AugmentImpl augment = (AugmentImpl)o;
            return this.getTargetPath().equals(augment.getTargetPath());
        }
    }

    public int hashCode() {
        return Objects.hash(schemaNodeContainer,
                getTargetPath());
    }

    protected ValidatorResult validateSelf() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
        if (this.isMandatory()) {
            SchemaNode mandatoryDescendant = this.getMandatoryDescendant();
            Severity severity = Severity.ERROR;
            if (mandatoryDescendant instanceof WhenSupport) {
                WhenSupport whenSupport = (WhenSupport)mandatoryDescendant;
                if (whenSupport.getWhen() != null) {
                    severity = Severity.WARNING;
                }
            }
            validatorResultBuilder.addRecord(
                    ModelUtil.reportError(mandatoryDescendant,severity, ErrorTag.BAD_ELEMENT,
                            ErrorCode.AUGMENT_MANDATORY_NODE.getFieldName()));

        }

        return validatorResultBuilder.build();
    }

    public List<YangStatement> getEffectiveSubStatements() {
        List<YangStatement> statements = new ArrayList();
        statements.addAll(getEffectiveSchemaNodeChildren());
        statements.addAll(super.getEffectiveSubStatements());
        return statements;
    }
    @Override
    public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
        return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
    }

}
