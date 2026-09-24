package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.impl.util.NetconfSelectUtil;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

public class AnyDataDataImpl extends YangDataImpl<Anydata> implements AnyDataData {
    private YangDataDocument value;

    public AnyDataDataImpl(Anydata schemaNode) {
        super(schemaNode);
        this.identifier = new SingleInstanceDataIdentifier(getQName());
    }

    @Override
    public YangDataDocument getValue() {
        return value;
    }

    @Override
    public YangDataDocument getEffectiveValue() {
        return NetconfSelectUtil.getEffectiveValue(this);
    }

    @Override
    public void setValue(YangDataDocument value) {
        this.value = value;
    }

    @Override
    public ValidatorResult validate() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        validatorResultBuilder.merge(super.validate());
        if (value != null) {
            mergeNestedValidationResult(validatorResultBuilder, value.validate());
        }
        return validatorResultBuilder.build();
    }

    private void mergeNestedValidationResult(
            ValidatorResultBuilder validatorResultBuilder,
            ValidatorResult nestedResult) {
        if (nestedResult == null || nestedResult.getRecords() == null) {
            return;
        }
        for (ValidatorRecord<?, ?> record : nestedResult.getRecords()) {
            validatorResultBuilder.addRecord(rebaseRecord(record));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ValidatorRecord rebaseRecord(ValidatorRecord<?, ?> record) {
        ValidatorRecordBuilder recordBuilder = new ValidatorRecordBuilder();
        recordBuilder.setSeverity(record.getSeverity());
        recordBuilder.setErrorTag(record.getErrorTag());
        recordBuilder.setErrorAppTag(record.getErrorAppTag());
        recordBuilder.setErrorPath(rebasePath(record.getErrorPath()));
        recordBuilder.setBadElement(record.getBadElement());
        recordBuilder.setErrorMessage(record.getErrorMsg());
        return recordBuilder.build();
    }

    private Object rebasePath(Object nestedPath) {
        if (!(nestedPath instanceof AbsolutePath)) {
            return nestedPath;
        }
        AbsolutePath rebasedPath = new AbsolutePath();
        addSteps(rebasedPath, buildAnydataPath());
        addSteps(rebasedPath, (AbsolutePath) nestedPath);
        return rebasedPath;
    }

    private AbsolutePath buildAnydataPath() {
        AbsolutePath anydataPath = new AbsolutePath();
        YangDataContainer parent = getContext().getParent();
        if (parent instanceof YangData) {
            YangData<?> parentData = (YangData<?>) parent;
            if (parentData.getContext().getParent() != null) {
                addSteps(anydataPath, parentData.getPath());
            }
        }
        anydataPath.addStep(new XPathStep(getQName()));
        return anydataPath;
    }

    private void addSteps(AbsolutePath destination, AbsolutePath source) {
        if (source == null || source.getSteps() == null) {
            return;
        }
        for (XPathStep step : source.getSteps()) {
            destination.addStep(step);
        }
    }
}
