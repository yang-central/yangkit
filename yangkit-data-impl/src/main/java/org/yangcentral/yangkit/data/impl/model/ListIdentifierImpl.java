package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.ListIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListIdentifierImpl extends DataIdentifierImpl implements ListIdentifier {
    private List<LeafData> keys;
    public ListIdentifierImpl(QName qName, List<LeafData> keys) {
        super(qName);
        if (null == keys) {
            this.keys = new ArrayList<>();
        } else {
            this.keys = keys;
        }
    }

    @Override
    public int compareTo(DataIdentifier o) {
        int compare = super.compareTo(o);
        if (0 != compare) {
            return compare;
        }
        if (!(o instanceof ListIdentifierImpl)) {
            throw new IllegalArgumentException("incompatible identifier");
        }
        ListIdentifierImpl oId = (ListIdentifierImpl) o;
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            LeafData thisData = keys.get(i);
            LeafData otherData = oId.keys.get(i);
            compare = thisData.compareTo(otherData);
            if (0 != compare) {
                return compare;
            }
        }
        return 0;
    }

    @Override
    public List<LeafData> getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getQName().getQualifiedName());
        for (LeafData leafData : keys) {
            sb.append("[");
            sb.append(leafData.getQName().getQualifiedName());
            sb.append("=");
            sb.append(leafData.getStringValue());
            sb.append("]");

        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListIdentifierImpl)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ListIdentifierImpl that = (ListIdentifierImpl) o;
        return Objects.equals(getKeys(), that.getKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getKeys());
    }
}
