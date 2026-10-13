package org.yangcentral.yangkit.model.api.stmt.ext;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.*;

public interface YangStructure extends MustSupport, GroupingDefContainer,SchemaNode,
        TypedefContainer, DataDefContainer, SchemaNodeContainer,Augmentable,YangUnknown{
    QName YANG_KEYWORD = new QName("urn:ietf:params:xml:ns:yang:ietf-yang-structure-ext","sx","structure");
    @Override
    default QName getYangKeyword() {
        return YANG_KEYWORD;
    }
}
