package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.LeafListIdentifier;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LeafListIdentifierImpl that = (LeafListIdentifierImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getQName().getQualifiedName());
        sb.append("{").append("value='" + value + '\'').append("}");
        return sb.toString();
    }
}
