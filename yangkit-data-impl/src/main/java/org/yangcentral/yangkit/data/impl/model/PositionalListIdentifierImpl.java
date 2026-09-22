package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.PositionalListIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PositionalListIdentifierImpl extends DataIdentifierImpl
        implements PositionalListIdentifier {
    private final int position;

    public PositionalListIdentifierImpl(QName qName, int position) {
        super(qName);
        if (position < 1) {
            throw new IllegalArgumentException("position must be greater than zero");
        }
        this.position = position;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public List<LeafData> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public int compareTo(DataIdentifier other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        if (!(other instanceof PositionalListIdentifier)) {
            throw new IllegalArgumentException("incompatible identifier");
        }
        return Integer.compare(position, ((PositionalListIdentifier) other).getPosition());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PositionalListIdentifier)) {
            return false;
        }
        PositionalListIdentifier that = (PositionalListIdentifier) other;
        return super.equals(other) && position == that.getPosition();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position);
    }

    @Override
    public String toString() {
        return getQName().getQualifiedName() + "[" + position + "]";
    }
}
