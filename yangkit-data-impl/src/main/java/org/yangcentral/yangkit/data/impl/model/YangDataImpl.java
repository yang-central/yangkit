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


public abstract class YangDataImpl<S extends SchemaNode> implements YangData<S> {
    private QName qName;
    private S schemaNode;

    private YangDataContext context;

    private List<Attribute> attributes = new ArrayList<>();

    private boolean dummyNode;
    private boolean validatingWhen;

    private Logger logger = LoggerFactory.getLogger(YangDataImpl.class);

    public YangDataImpl(S schemaNode) {
        this.schemaNode = schemaNode;
        this.qName = schemaNode.getIdentifier();
    }

    public YangDataContext getContext() {
        return context;
    }

    public void setContext(YangDataContext context) {
        this.context = context;
    }

    public QName getQName() {
        return qName;
    }

    @Override
    public S getSchemaNode() {
        return schemaNode;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
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

    @Override
    public void update() {
        if(this instanceof YangDataContainer){
            YangDataContainer yangDataContainer = (YangDataContainer) this;
            if(yangDataContainer.getChildren() != null){
                for(YangData<?> child: yangDataContainer.getChildren()){
                    if(null == child){
                        continue;
                    }
                    child.getContext().setDocument(this.getContext().getDocument());
                    child.update();
                }
            }
        }
    }

    @Override
    public void addAttribute(Attribute attribute) {
        if (null != getAttribute(attribute.getName())) {
            return;
        }

        attributes.add(attribute);
    }

    @Override
    public Attribute getAttribute(QName qName) {
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().equals(qName)) {
                return attribute;
            }
        }
        return null;
    }

    @Override
    public List<Attribute> getAttributes(String name) {
        List<Attribute> candidate = null;
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().getLocalName().equals(name)) {
                if (null == candidate) {
                    candidate = new ArrayList<>();
                }
                candidate.add(attribute);
            }
        }
        return candidate;
    }

    @Override
    public void deleteAttribute(QName qName) {
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().equals(qName)) {
                attributes.remove(attribute);
                return;
            }
        }
        return;
    }

    @Override
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
    private  XPathStep translate2Step(YangData<? extends DataNode> yangData) {
        XPathStep step = new XPathStep(yangData.getQName());
        if (yangData.getIdentifier() instanceof ListIdentifier) {
            ListIdentifier listIdentifier = (ListIdentifier) yangData.getIdentifier();
            List<LeafData<?>> keys = listIdentifier.getKeys();
            for (LeafData<?> key : keys) {
                Predict predict = new Predict(key.getQName(), key.getStringValue());
                step.addPredict(predict);
            }
        } else if (yangData.getIdentifier() instanceof LeafListIdentifier) {
            LeafListIdentifier leafListIdentifier = (LeafListIdentifier) (yangData.getIdentifier());
            Predict predict = new Predict(leafListIdentifier.getQName(), ((TypedData<?, ?>) yangData).getStringValue());
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
        DataDefinition dataDefinition = (DataDefinition) getSchemaNode();
        When when = dataDefinition.getWhen();

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

    private boolean findValueMatched(List<YangData<?>> dataList, TypedData<?, ?> candidate) {
        if (null == dataList || dataList.size() == 0) {
            return false;
        }
        for (YangData<?> data : dataList) {
            if (null == data) {
                continue;
            }
            if (data instanceof TypedData) {
                TypedData<?, ?> typedData = (TypedData<?, ?>) data;
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
            boolean findMatched = findValueMatched(nodeSet, (TypedData<?,?>) this);
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
        if (getSchemaNode() instanceof MustSupport) {
            MustSupport mustConstraintAware = (MustSupport) getSchemaNode();
            validatorResultBuilder.merge(validateMust(mustConstraintAware));
        }

        if (this instanceof TypedData) {
            TypedData<?, ?> typedData = (TypedData<?, ?>) this;
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

    @Override
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
    public YangData<S> clone() throws CloneNotSupportedException {
        YangData<S> cloned = (YangData<S>) super.clone();
        if (null != getAttributes()) {
            for (Attribute attribute : getAttributes()) {
                if (null == attribute) {
                    continue;
                }
                cloned.addAttribute(attribute.clone());
            }
        }
        cloned.update();
        return cloned;
    }

    @Override
    public List<YangCompareResult> compare(YangData<?> data) {
        if (!getIdentifier().equals(data.getIdentifier())) {
            throw new IllegalArgumentException("the candidate data must have same identifier.");
        }
        List<YangCompareResult> results = new ArrayList<>();
        if (this instanceof TypedData) {
            if (!equals(data)) {
                results.add(new YangCompareResultImpl(getPath(), DifferenceType.CHANGED, data));
                return results;
            }
        } else if (this instanceof YangDataContainer) {
            List<YangData<?extends DataNode>> children = ((YangDataContainer) this).getDataChildren();
            boolean matchArray[] = null;
            if (null != children) {
                matchArray = new boolean[children.size()];
            }
            List<YangData<? extends DataNode>> candidateChildren = ((YangDataContainer) data).getDataChildren();
            boolean matchCandidateArray[] = null;
            if (null != candidateChildren) {
                matchCandidateArray = new boolean[candidateChildren.size()];
            }
            if (null != children && null == candidateChildren) {
                // all none
                for (int i = 0; i < children.size(); i++) {
                    YangData<?> child = children.get(i);
                    results.add(new YangCompareResultImpl(getPath(), DifferenceType.NONE, child));
                }
            } else if (null == children && null != candidateChildren) {
                // all new
                for (int i = 0; i < candidateChildren.size(); i++) {
                    YangData<?> candidateChild = candidateChildren.get(i);
                    results.add(new YangCompareResultImpl(getPath(), DifferenceType.NEW, candidateChild));
                }
            } else if (null != children && null != candidateChildren) {
                for (int i = 0; i < children.size(); i++) {
                    YangData<?> child = children.get(i);
                    for (int j = 0; j < candidateChildren.size(); j++) {
                        if (matchCandidateArray[j]) {
                            continue;
                        }
                        YangData<?> candidateChild = candidateChildren.get(j);
                        if (child.getIdentifier().equals(candidateChild.getIdentifier())) {
                            matchArray[i] = true;
                            matchCandidateArray[j] = true;
                            List<YangCompareResult> childResults = child.compare(candidateChild);
                            if (0 != childResults.size()) {
                                results.addAll(childResults);
                            }
                            break;
                        }
                    }
                    // if no match, this child shouble be none in candidate
                    if (!matchArray[i]) {
                        results.add(new YangCompareResultImpl(getPath(), DifferenceType.NONE, child));
                    }
                }
                // process no matched children of candidate, these children should be new in this
                for (int i = 0; i < candidateChildren.size(); i++) {
                    if (matchCandidateArray[i]) {
                        continue;
                    }
                    YangData<?> candidateChild = candidateChildren.get(i);
                    results.add(new YangCompareResultImpl(getPath(), DifferenceType.NEW, candidateChild));
                }

            }

        }

        return results;
    }

    @Override
    public boolean isConfig() {
        return getSchemaNode().isConfig();
    }

    @Override
    public boolean isDummyNode() {
        return dummyNode;
    }

    @Override
    public void setDummyNode(boolean bool) {
        this.dummyNode = bool;
    }
}
