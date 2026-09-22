package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.List;
import java.util.Objects;

public class ListDataImpl extends YangDataContainerImpl<YangList> implements ListData {
    private List<LeafData> keys;
    public ListDataImpl(YangList schemaNode,List<LeafData> keys) {
        super(schemaNode);
        if (schemaNode.getKey() == null || schemaNode.getKey().getkeyNodes().isEmpty()) {
            this.keys = java.util.Collections.emptyList();
            identifier = new PositionalListIdentifierImpl(schemaNode.getIdentifier(), 1);
        } else {
            this.keys = keys;
            identifier = new ListIdentifierImpl(schemaNode.getIdentifier(),keys);
        }
    }

    @Override
    public List<LeafData> getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ListDataImpl listData = (ListDataImpl) o;
        return Objects.equals(keys, listData.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keys);
    }
}
