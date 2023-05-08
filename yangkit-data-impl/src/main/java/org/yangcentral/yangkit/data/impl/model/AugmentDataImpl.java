package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.AugmentData;
import org.yangcentral.yangkit.model.api.stmt.Augment;

public class AugmentDataImpl extends YangDataContainerImpl<Augment> implements AugmentData {
    public AugmentDataImpl(Augment schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(getQName());

    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
