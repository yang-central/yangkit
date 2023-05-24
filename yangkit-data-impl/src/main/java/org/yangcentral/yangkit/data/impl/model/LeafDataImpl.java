package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;

public class LeafDataImpl extends YangDataImpl<Leaf> implements LeafData {
    private YangDataValue<?,?> value;
    public LeafDataImpl(Leaf schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public YangDataValue<?,?> getValue() {
        return this.value;
    }

    @Override
    public String getStringValue() {
        return value.getStringValue();
    }

    @Override
    public void setValue(YangDataValue<?, ?> value) {
        this.value = value;
    }
}
