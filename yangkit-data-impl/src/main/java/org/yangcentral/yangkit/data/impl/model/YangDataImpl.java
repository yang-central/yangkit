package org.yangcentral.yangkit.data.impl.model;

import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.common.api.*;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.base.YangDataContext;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.util.YangDataUtil;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.xpath.YangContextSupport;
import org.yangcentral.yangkit.xpath.YangXPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public abstract class YangDataImpl<S extends SchemaNode> extends YangAbstractDataEntry<YangData> implements YangData<S> {

    private S schemaNode;

    private YangDataContext context;

    private boolean dummyNode;
    private boolean validatingWhen;

    protected DataIdentifier identifier;

    private Logger logger = LoggerFactory.getLogger(YangDataImpl.class);

    public YangDataImpl(S schemaNode) {
        super(schemaNode.getIdentifier());
        this.schemaNode = schemaNode;
        this.context = new YangDataContext(this);
    }

    public YangDataContext getContext() {
        return context;
    }

    public void setContext(YangDataContext context) {
        this.context = context;
    }



    @Override
    public S getSchemaNode() {
        return schemaNode;
    }


    @Override
    public boolean isRoot() {
        return getContext().getDataParent() == null?true:false;
    }

    @Override
    public void detach() {
        getContext().setParent(null);
        getContext().setDocument(null);
        update();
    }




    private  XPathStep translate2Step(YangData<? extends DataNode> yangData) {
        XPathStep step = new XPathStep(yangData.getQName());
        if (yangData.getIdentifier() instanceof ListIdentifier) {
            ListIdentifier listIdentifier = (ListIdentifier) yangData.getIdentifier();
            List<LeafData> keys = listIdentifier.getKeys();
            for (LeafData key : keys) {
                Predict predict = new Predict(key.getQName(), key.getStringValue());
                step.addPredict(predict);
            }
        } else if (yangData.getIdentifier() instanceof LeafListIdentifier) {
            LeafListIdentifier leafListIdentifier = (LeafListIdentifier) (yangData.getIdentifier());
            Predict predict = new Predict(leafListIdentifier.getQName(), ((TypedData<?>) yangData).getStringValue());
            step.addPredict(predict);
        }
        return step;
    }
    @Override
    public AbsolutePath getPath() {
        AbsolutePath parentPath;
        YangDataContainer parent = this.getContext().getParent();
        if(parent == null){
            return null;
        }
        if(parent instanceof YangDataDocument){
            parentPath = new AbsolutePath();
        }
        else {
            parentPath = ((YangData)parent).getPath();
        }
        if(this.isVirtual()){
            return parentPath;
        }
        parentPath.addStep(translate2Step((YangData<? extends DataNode>) this));
        return parentPath;
    }

    @Override
    public boolean checkWhen() throws JaxenException {
        SchemaNode dataDefinition = getSchemaNode();
        if(!(dataDefinition instanceof WhenSupport)){
            return true;
        }
        When when = ((WhenSupport)dataDefinition).getWhen();

        if (when == null) {
            return true;
        }
        if(validatingWhen){
            throw new IllegalArgumentException(
                    "loop when condition detected.when condition:" + when.getArgStr());
        }
        validatingWhen = true;
        Object contextNode = YangDataUtil.getXpathContextData(this);
        try {
            YangXPath yangXPath = when.getXPathExpression();
            Object result = yangXPath.evaluate(contextNode, YangContextSupport.EvaluateType.NEST);
            Boolean bool = YangDataUtil.getXPathBooleanValue(result);
            validatingWhen = false;
            return bool;
        } catch (Exception e){
            validatingWhen = false;
            throw e;
        }

    }

    private boolean findValueMatched(List<YangData<?>> dataList, TypedData<?> candidate) {
        if (null == dataList || dataList.size() == 0) {
            return false;
        }
        for (YangData<?> data : dataList) {
            if (null == data) {
                continue;
            }
            if (data instanceof TypedData) {
                TypedData<?> typedData = (TypedData<?>) data;
                if (typedData.getValue().equals(candidate.getValue())) {
                    return true;
                }
            }

        }
        return false;
    }

    private ValidatorResult validateMust(MustSupport mustSupport){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        Iterator<? extends Must> musts = mustSupport.getMusts().iterator();
        while (musts.hasNext()) {
            Must mustDefinition = musts.next();

            YangXPath yangDataXPath = mustDefinition.getXPathExpression();
            try {
                Object result = yangDataXPath.evaluate(YangDataUtil.getXpathContextData(this));
                Boolean bool = YangDataUtil.getXPathBooleanValue(result);
                if (!bool) {
                    yangDataXPath.evaluate(YangDataUtil.getXpathContextData(this));
                    ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder =
                            new ValidatorRecordBuilder<>();
                    validatorRecordBuilder.setErrorPath(getPath());
                    validatorRecordBuilder.setBadElement(this);
                    validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                    String errMsg = " failed to validate must:" + mustDefinition.getArgStr();
                    validatorRecordBuilder.setErrorMessage(new ErrorMessage(errMsg));
                    if (mustDefinition.getErrorAppTag() != null) {
                        validatorRecordBuilder.setErrorAppTag(mustDefinition.getErrorAppTag().getArgStr());
                    }

                    ValidatorRecord record = validatorRecordBuilder.build();
                    logger.error(record.toString());
                    validatorResultBuilder.addRecord(record);
                }
            } catch (JaxenException | RuntimeException e) {
                ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder =
                        new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setErrorPath(getPath());
                String errMsg = null;
                if (e instanceof JaxenException) {
                    errMsg = "must definition:" + mustDefinition.getArgStr() + " failed to be parsed.";
                } else {
                    errMsg = "error occurs when evaluate must definition:" + mustDefinition.getArgStr() + ". detail error: "
                            + ((RuntimeException) e).getMessage();
                }
                validatorRecordBuilder.setErrorMessage(new ErrorMessage(errMsg));
                validatorRecordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
                ValidatorRecord record = validatorRecordBuilder.build();
                logger.error(record.toString());
                validatorResultBuilder.addRecord(record);
            }
        }
        return  validatorResultBuilder.build();
    }

    private ValidatorResult validateLeafRef(LeafRef leafRef){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        try {

            YangXPath yangDataXPath = leafRef.getEffectivePath()
                    .getXPathExpression();
            Object result = yangDataXPath.evaluate(YangDataUtil.getXpathContextData(this));
            List<YangData<?>> nodeSet = (List<YangData<?>>) result;
            boolean findMatched = findValueMatched(nodeSet, (TypedData<?>) this);
            if (!findMatched) {
                ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder =
                        new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setErrorPath(getPath());
                validatorRecordBuilder.setBadElement(this);
                validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                String errMsg = "The data node referenced by path:" + leafRef.getEffectivePath().getArgStr() + " is not exists,";
                validatorRecordBuilder.setErrorMessage(new ErrorMessage(errMsg));
                ValidatorRecord record = validatorRecordBuilder.build();
                logger.error(record.toString());
                validatorResultBuilder.addRecord(record);
            }
        } catch (JaxenException | IllegalArgumentException e) {
            ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder =
                    new ValidatorRecordBuilder<>();
            validatorRecordBuilder.setErrorPath(getPath());
            String errMsg = "path definition:" + leafRef.getEffectivePath().getArgStr() + " failed to be parsed.";
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(errMsg));
            validatorRecordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
            ValidatorRecord record = validatorRecordBuilder.build();
            logger.error(record.toString());
            validatorResultBuilder.addRecord(record);
        }
        return validatorResultBuilder.build();
    }

    @Override
    public ValidatorResult validate() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        //check when
        validatorResultBuilder.merge(processWhen());

        if (getSchemaNode() instanceof MustSupport) {
            MustSupport mustConstraintAware = (MustSupport) getSchemaNode();
            validatorResultBuilder.merge(validateMust(mustConstraintAware));
        }

        if (this instanceof TypedData) {
            TypedData<?> typedData = (TypedData<?>) this;
            // check value
            if (typedData.getValue() == null) {
                ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder =
                        new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setErrorPath(getPath());
                validatorRecordBuilder.setBadElement(this);
                validatorRecordBuilder.setErrorTag(ErrorTag.INVALID_VALUE);
                String errMsg = "leaf or leaf-list data should have value.";
                validatorRecordBuilder.setErrorMessage(new ErrorMessage(errMsg));
                ValidatorRecord record = validatorRecordBuilder.build();
                logger.error(record.toString());
                validatorResultBuilder.addRecord(record);
            }
            if (typedData.getSchemaNode().getType().getRestriction() instanceof LeafRef) {
                LeafRef leafrefTypeDefinition =
                        (LeafRef) typedData.getSchemaNode().getType().getRestriction();
                validatorResultBuilder.merge(validateLeafRef(leafrefTypeDefinition));

            }
        }
        return validatorResultBuilder.build();
    }

    public ValidatorResult processWhen() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        try {
            if (!checkWhen()) {
                if (this.isDummyNode()) {
                    YangDataContainer yangDataContainer = null;
                    if (this.getContext().getParent() != null) {
                        yangDataContainer = (YangDataContainer) this.getContext().getParent();
                    } else {
                        yangDataContainer = this.getContext().getDocument();
                    }
                    yangDataContainer.removeChild(this.getIdentifier());
                } else {
                    //checkWhen();
                    ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder
                            = new ValidatorRecordBuilder<>();
                    validatorRecordBuilder.setErrorPath(this.getPath());
                    validatorRecordBuilder.setBadElement(this);
                    validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                    String errorMsg = "the node should not exist,"
                            + "because when condition's validation result is false. condition:"
                            + ((DataDefinition) schemaNode).getWhen().getArgStr();
                    validatorRecordBuilder.setErrorMessage(new ErrorMessage(errorMsg));
                    ValidatorRecord record = validatorRecordBuilder.build();
                    logger.error(record.toString());
                    validatorResultBuilder.addRecord(record);
                }
            }
        } catch (JaxenException | RuntimeException e) {
            ValidatorRecordBuilder<AbsolutePath, YangData<?>> validatorRecordBuilder = new ValidatorRecordBuilder<>();
            validatorRecordBuilder.setErrorPath(this.getPath());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(e.getMessage()));
            validatorRecordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
            ValidatorRecord record = validatorRecordBuilder.build();
            logger.error(record.toString());
            validatorResultBuilder.addRecord(record);
        }
        return validatorResultBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof YangDataImpl)) {
            return false;
        }
        YangDataImpl<?> yangData = (YangDataImpl<?>) o;
        return getSchemaNode().equals(yangData.getSchemaNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSchemaNode());
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }


    @Override
    public List<YangDataCompareResult> compare(YangData data) {
        if (!getIdentifier().equals(data.getIdentifier())) {
            throw new IllegalArgumentException("the candidate data must have same identifier.");
        }
        List<YangDataCompareResult> results = new ArrayList<>();
        if (this instanceof TypedData) {
            if (!equals(data)) {
                results.add(new YangCompareResultImpl(getPath(), DifferenceType.CHANGED, data));
                return results;
            }
        }

        return results;
    }

    @Override
    public boolean isConfig() {
        return getSchemaNode().isConfig();
    }

    @Override
    public boolean isMandatory() {

        try {
            if(!checkWhen()){
                return false;
            }
        } catch (JaxenException e) {

        }
        if(!schemaNode.isMandatory()){
            return false;
        }
        if(this instanceof YangDataContainer){
            YangDataContainer yangDataContainer = (YangDataContainer) this;
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) this.schemaNode;
            for(SchemaNode childSchemaNode : schemaNodeContainer.getSchemaNodeChildren()){
                List<YangData<?>> matched = yangDataContainer.getChildren(childSchemaNode.getIdentifier());
                if(!matched.isEmpty()){
                    for(YangData<?> childData:matched){
                        if(childData.isMandatory()){
                            return true;
                        }
                    }
                } else {
                    YangData<?> dummyNode = YangDataBuilderFactory.getBuilder().getYangData(childSchemaNode,null);
                    try {
                        yangDataContainer.addChild(dummyNode,true);
                        if(dummyNode.isMandatory()){
                            yangDataContainer.removeChild(dummyNode.getIdentifier());
                            return true;
                        }
                    } catch (YangDataException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDummyNode() {
        return dummyNode;
    }

    @Override
    public void setDummyNode(boolean bool) {
        this.dummyNode = bool;
    }

    @Override
    public DataIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public YangData clone() throws CloneNotSupportedException {
        YangData cloned =  super.clone();
        cloned.setContext(new YangDataContext(cloned));
        if(this instanceof YangDataContainer){
            YangDataContainer yangDataContainer = (YangDataContainer) this;
            for(YangData<?> child:yangDataContainer.getDataChildren()){
                YangData<?> childCloned = child.clone();
                ((YangDataContainer)cloned).removeChild(child.getIdentifier());
                try {
                    ((YangDataContainer)cloned).addChild(childCloned);
                } catch (YangDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return cloned;

    }
}
