package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface YangList extends MultiInstancesDataNode, ContainerDataNode {
   Key getKey();

   List<Unique> getUniques();

   Unique getUnique(String var1);

   ValidatorResult addUnique(Unique var1);

   void removeUnique(String var1);

   ValidatorResult updateUnique(Unique var1);
}
