package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.List;

public class ListDataImpl extends YangDataContainerImpl<YangList> implements ListData {
    private List<LeafData> keys;
    public ListDataImpl(YangList schemaNode,List<LeafData> keys) {
        super(schemaNode);
        this.keys = keys;
        identifier = new ListIdentifierImpl(schemaNode.getIdentifier(),keys);
    }

    @Override
    public List<LeafData> getKeys() {
        return keys;
    }
}
