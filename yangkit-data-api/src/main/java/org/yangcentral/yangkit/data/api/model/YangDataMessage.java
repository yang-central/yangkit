package org.yangcentral.yangkit.data.api.model;

public interface YangDataMessage<T extends YangDataMessage> extends YangDataEntity<T> {
    YangDataDocument getDocument();
    void setDocument(YangDataDocument document);
}
