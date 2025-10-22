package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Rpc;

public interface RpcData extends YangData<Rpc>{
    InputData getInput();
    void setInput(InputData input);
    OutPutData getOutPut();
    void setOutput(OutPutData output);
}
