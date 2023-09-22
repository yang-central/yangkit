package org.yangcentral.yangkit.data.api.model;

public interface YangDataMessage<T extends YangDataMessage> extends YangDataEntity<T> {
    YangDataDocument getBody();
    void setBody(YangDataDocument document);
}
