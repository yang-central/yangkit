package org.yangcentral.yangkit.model.api.stmt.ext;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.*;

public interface AugmentStructure extends YangUnknown, Entity, DataDefContainer, DataNodeModifier,
        VirtualSchemaNode {
    QName YANG_KEYWORD = new QName("urn:ietf:params:xml:ns:yang:ietf-yang-structure-ext","sx","augment-structure");
    @Override
    default QName getYangKeyword() {
        return YANG_KEYWORD;
    }
}
