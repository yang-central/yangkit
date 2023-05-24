package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.model.api.stmt.Container;

public class ContainerDataImpl extends YangDataContainerImpl<Container> implements ContainerData {
    public ContainerDataImpl(Container schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }
}
