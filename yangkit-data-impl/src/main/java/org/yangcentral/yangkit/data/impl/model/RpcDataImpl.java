package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.stmt.Rpc;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.List;

public class RpcDataImpl extends YangDataImpl<Rpc> implements RpcData {

    private InputData input;
    private OutPutData output;

    public RpcDataImpl(Rpc schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public InputData getInput() {
        return input;
    }

    @Override
    public void setInput(InputData input) {
        this.input = input;
    }

    @Override
    public OutPutData getOutPut() {
        return output;
    }

    @Override
    public void setOutput(OutPutData output) {
        this.output = output;
    }
}
