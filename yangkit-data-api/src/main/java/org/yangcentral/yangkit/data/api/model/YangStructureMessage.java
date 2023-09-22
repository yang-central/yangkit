package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

public interface YangStructureMessage<T extends YangStructureMessage> extends YangDataMessage<T>{
    YangStructure getStructure();
    YangDataDocument getStructureData();
    void setStructureData(YangDataDocument structureData);
}
