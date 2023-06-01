package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

public interface YangStructureMessage extends YangDataMessage<YangStructureMessage>{
    YangDataStructure getStructure();
}
