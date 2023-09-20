package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

public interface YangStructureMessage<T extends YangStructureMessage> extends YangDataMessage<T>{
    YangDataStructure getStructure();
}
