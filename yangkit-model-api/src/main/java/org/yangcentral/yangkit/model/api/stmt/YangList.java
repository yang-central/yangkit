package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface YangList extends MultiInstancesDataNode, ContainerDataNode ,YangBuiltinStatement{
   Key getKey();

   List<Unique> getUniques();

   Unique getUnique(String unique);

   ValidatorResult addUnique(Unique unique);

   void removeUnique(String unique);

   ValidatorResult updateUnique(Unique unique);
}
