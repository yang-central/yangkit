package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.Input;
import org.yangcentral.yangkit.model.api.stmt.Output;

public class ActionDataImpl extends YangDataContainerImpl<Action> implements ActionData {
    private InputData input;
    private OutPutData output;

    public ActionDataImpl(Action schemaNode) {
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
    @Override
    public void addChild(YangData<?> child, boolean autoDelete) throws YangDataException {
        super.addChild(child, autoDelete);
        if(child.getSchemaNode() instanceof Input){
            setInput((InputData) child);
        } else if (child.getSchemaNode() instanceof Output) {
            setOutput((OutPutData) child);
        }
    }

    @Override
    public YangData<?> removeChild(DataIdentifier identifier) {
        if(identifier.getQName().getLocalName().equals("input")){
            setInput(null);
        } else if (identifier.getQName().getLocalName().equals("output")){
            setOutput(null);
        }
        return super.removeChild(identifier);
    }
}
