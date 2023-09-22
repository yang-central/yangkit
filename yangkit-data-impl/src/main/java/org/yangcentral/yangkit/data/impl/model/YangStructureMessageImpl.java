package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataCompareResult;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.data.api.model.YangStructureMessage;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.List;

public class YangStructureMessageImpl<T extends YangStructureMessage> extends YangDataMessageImpl<T> implements YangStructureMessage<T> {
    YangStructure structure;
    YangDataDocument structureData;
    YangDataDocument body;
    public YangStructureMessageImpl(YangStructure structure) {
        super(structure.getIdentifier());
        this.structure = structure;
    }

    @Override
    public ValidatorResult validate() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(structureData.validate());
        validatorResultBuilder.merge(body.validate());
        return validatorResultBuilder.build();
    }

    @Override
    public List<YangDataCompareResult> compare(YangStructureMessage other) {
        List<YangDataCompareResult> result = structureData.compare(other.getStructureData());
        result.addAll(body.compare(other.getBody()));
        return result;
    }

    @Override
    public YangDataDocument getBody() {
        return body;
    }

    @Override
    public void setBody(YangDataDocument document) {
        this.body = document;
    }

    @Override
    public YangStructure getStructure() {
        return structure;
    }

    @Override
    public YangDataDocument getStructureData() {
        return structureData;
    }

    public void setStructureData(YangDataDocument structureData) {
        this.structureData = structureData;
    }
}
