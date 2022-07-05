package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface LeafList extends MultiInstancesDataNode, TypedDataNode {
   List<Default> getDefaults();

   List<Default> getEffectiveDefaults();

   void setDefaults(List<Default> var1);

   Default getDefault(String var1);

   ValidatorResult addDefault(Default var1);

   ValidatorResult updateDefault(Default var1);

   void removeDefault(String var1);
}
