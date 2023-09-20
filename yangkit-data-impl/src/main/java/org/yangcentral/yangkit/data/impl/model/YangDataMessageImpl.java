package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.model.YangDataCompareResult;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangDataMessage;

import java.util.List;

public class YangDataMessageImpl<T extends YangDataMessage> extends YangAbstractDataEntry<T> implements YangDataMessage<T> {
    private YangDataDocument content;
    public YangDataMessageImpl(QName qName) {
        super(qName);
    }

    @Override
    public ValidatorResult validate() {
        return null;
    }

    @Override
    public List<YangDataCompareResult> compare(T other) {
        return null;
    }

    @Override
    public YangDataDocument getDocument() {
        return content;
    }

    @Override
    public void setDocument(YangDataDocument document) {
        this.content = document;
    }
}
