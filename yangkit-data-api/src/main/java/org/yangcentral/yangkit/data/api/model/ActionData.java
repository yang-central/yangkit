package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Action;

public interface ActionData extends YangData<Action>,YangDataContainer{
    InputData getInput();
    void setInput(InputData input);
    OutPutData getOutPut();
    void setOutput(OutPutData output);
}
