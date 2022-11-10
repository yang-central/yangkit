package org.yangcentral.yangkit.model.impl.stmt;


import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

public class RpcImpl extends OperationImpl implements Rpc {
   public RpcImpl(String argStr) {
      super(argStr);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.RPC.getQName();
   }
}
