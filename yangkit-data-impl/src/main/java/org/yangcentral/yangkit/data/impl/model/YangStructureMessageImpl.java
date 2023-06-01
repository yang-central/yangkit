package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.model.YangDataCompareResult;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangStructureMessage;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

import java.util.List;

public class YangStructureMessageImpl extends YangAbstractDataEntry<YangStructureMessage> implements YangStructureMessage {
    YangDataStructure structure;
    YangDataDocument document;
    public YangStructureMessageImpl(YangDataStructure structure) {
        super(structure.getIdentifier());
        this.structure = structure;
    }

    @Override
    public ValidatorResult validate() {
        return null;
    }

    @Override
    public List<YangDataCompareResult> compare(YangStructureMessage other) {
        return null;
    }

    @Override
    public YangDataDocument getDocument() {
        return null;
    }

    @Override
    public void setDocument(YangDataDocument document) {

    }

    @Override
    public YangDataStructure getStructure() {
        return structure;
    }
}
