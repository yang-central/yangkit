package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.InputData;
import org.yangcentral.yangkit.model.api.stmt.Input;

public class InputDataImpl extends YangDataContainerImpl<Input> implements InputData {
    public InputDataImpl(Input schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }
}
