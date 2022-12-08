package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;

public class SingleInstanceDataIdentifier extends DataIdentifierImpl{

    public SingleInstanceDataIdentifier(QName qName) {
        super(qName);
    }

    @Override
    public String toString() {
        return getQName().getQualifiedName();
    }
}
