package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.AugmentStructureData;
import org.yangcentral.yangkit.model.api.stmt.ext.AugmentStructure;

public class AugmentStructureDataImpl extends YangDataContainerImpl<AugmentStructure> implements AugmentStructureData {
    public AugmentStructureDataImpl(AugmentStructure schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
