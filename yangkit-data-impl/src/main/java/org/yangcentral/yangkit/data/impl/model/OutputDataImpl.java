package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.OutPutData;
import org.yangcentral.yangkit.model.api.stmt.Output;

public class OutputDataImpl extends YangDataContainerImpl<Output> implements OutPutData {
    public OutputDataImpl(Output schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }
}
