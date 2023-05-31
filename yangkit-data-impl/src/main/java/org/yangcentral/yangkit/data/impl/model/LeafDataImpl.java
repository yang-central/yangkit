package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LeafDataImpl leafData = (LeafDataImpl) o;
        return value.equals(leafData.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
