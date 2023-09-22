package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

public class YangStructureDataImpl extends YangDataContainerImpl<YangStructure> implements YangStructureData {
    public YangStructureDataImpl(YangStructure schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }
}
