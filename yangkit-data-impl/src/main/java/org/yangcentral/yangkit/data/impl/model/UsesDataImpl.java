package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.UsesData;
import org.yangcentral.yangkit.model.api.stmt.Uses;

public class UsesDataImpl extends YangDataContainerImpl<Uses> implements UsesData {
    public UsesDataImpl(Uses schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
