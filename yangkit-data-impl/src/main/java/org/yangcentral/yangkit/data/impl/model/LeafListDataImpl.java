package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

import javax.annotation.Nullable;
import java.util.Objects;

public class LeafListDataImpl extends YangDataImpl<LeafList>  implements LeafListData {
    YangDataValue<?,?> value;
    public LeafListDataImpl(LeafList schemaNode, @Nullable YangDataValue<?,?> value) {
        super(schemaNode);
        this.value = value;
        if(value == null){
            identifier = new LeafListIdentifierImpl(schemaNode.getIdentifier(),null);
        } else {
            identifier = new LeafListIdentifierImpl(schemaNode.getIdentifier(),value.getStringValue());
        }

    }

    @Override
    public YangDataValue getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return value.getStringValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LeafListDataImpl that = (LeafListDataImpl) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
