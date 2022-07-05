package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;

public interface TypedDataNode extends DataNode {
   Type getType();

   ValidatorResult setType(Type var1);

   Units getUnits();

   void setUnits(Units var1);
}
