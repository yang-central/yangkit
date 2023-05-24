package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

public class LeafListDataImpl extends YangDataImpl<LeafList>  implements LeafListData {
    YangDataValue<?,?> value;
    public LeafListDataImpl(LeafList schemaNode,YangDataValue<?,?> value) {
        super(schemaNode);
        this.value = value;
        identifier = new LeafListIdentifierImpl(schemaNode.getIdentifier(),value.getStringValue());
    }

    @Override
    public YangDataValue getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return value.getStringValue();
    }
}
