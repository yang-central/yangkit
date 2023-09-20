package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;

import java.util.Objects;

public class LeafDataImpl<D> extends YangDataImpl<Leaf> implements LeafData<D> {
    private YangDataValue<D,?> value;
    public LeafDataImpl(Leaf schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public YangDataValue<D,?> getValue() {
        return this.value;
    }

    @Override
    public String getStringValue()  {
        try {
            return value.getStringValue();
        } catch (YangCodecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getStringValue(StringValueCodec<D> codec) throws YangCodecException {
        return value.getStringValue(codec);
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

    @Override
    public void setValue(YangDataValue<D, ?> value) {
        this.value = value;
    }
}
