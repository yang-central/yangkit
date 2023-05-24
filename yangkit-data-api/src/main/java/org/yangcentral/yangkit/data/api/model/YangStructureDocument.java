package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

public interface YangStructureDocument extends YangDataDocument{
    YangDataStructure getStructure();
}
