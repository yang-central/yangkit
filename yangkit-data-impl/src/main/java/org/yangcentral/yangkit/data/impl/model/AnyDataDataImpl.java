package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

public class AnyDataDataImpl extends YangDataImpl<Anydata> implements AnyDataData {
    private YangDataDocument value;
    public AnyDataDataImpl(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    public YangDataDocument getValue() {
        return value;
    }

    @Override
    public void setValue(YangDataDocument value) {
        this.value = value;
    }

    @Override
    public DataIdentifier getIdentifier() {
        return null;
    }
}
