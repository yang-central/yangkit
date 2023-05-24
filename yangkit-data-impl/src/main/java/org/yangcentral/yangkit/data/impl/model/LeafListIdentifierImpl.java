package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.LeafListIdentifier;

public class LeafListIdentifierImpl extends DataIdentifierImpl implements LeafListIdentifier {
    String value;
    public LeafListIdentifierImpl(QName qName,String value) {
        super(qName);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
