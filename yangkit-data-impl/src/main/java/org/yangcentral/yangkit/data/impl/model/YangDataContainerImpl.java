package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.List;

public class YangDataContainerImpl<S extends SchemaNode> extends YangDataImpl<S> implements YangDataContainer {

    private YangAbstractDataContainer container;

    public YangDataContainerImpl(S schemaNode) {
        super(schemaNode);
        container = new YangAbstractDataContainer(this);
    }

    @Override
    public List<YangData<?>> getChildren() {
        return container.getChildren();
    }

    @Override
    public YangData<?> getChild(DataIdentifier identifier) {
        return container.getChild(identifier);
    }

    @Override
    public List<YangData<?>> getChildren(QName qName) {
        return container.getChildren(qName);
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren() {
        return container.getDataChildren();
    }

    @Override
    public YangData<? extends DataNode> getDataChild(DataIdentifier identifier) {
        return container.getDataChild(identifier);
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(QName qName) {
        return container.getDataChildren(qName);
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(String name) {
        return container.getDataChildren(name);
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(String name, String namespace) {
        return container.getDataChildren(name,namespace);
    }

    @Override
    public void addChild(YangData<?> child, boolean autoDelete) throws YangDataException {
        container.addChild(child,autoDelete);
    }

    @Override
    public YangData<?> removeChild(DataIdentifier identifier) {
        return container.removeChild(identifier);
    }

    @Override
    public void addDataChild(YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException {
        container.addDataChild(child,autoDelete);
    }

    @Override
    public YangData<? extends DataNode> removeDataChild(DataIdentifier identifier) {
        return container.removeDataChild(identifier);
    }
}
