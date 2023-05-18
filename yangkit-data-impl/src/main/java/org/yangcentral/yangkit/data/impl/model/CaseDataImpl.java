package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.CaseData;
import org.yangcentral.yangkit.model.api.stmt.Case;

public class CaseDataImpl extends YangDataContainerImpl<Case> implements CaseData {
    public CaseDataImpl(Case schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
