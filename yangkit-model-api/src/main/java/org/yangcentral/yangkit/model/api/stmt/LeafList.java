package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface LeafList extends MultiInstancesDataNode, TypedDataNode {
   List<Default> getDefaults();

   List<Default> getEffectiveDefaults();

   void setDefaults(List<Default> defaults);

   Default getDefault(String value);

   ValidatorResult addDefault(Default aDefault);

   ValidatorResult updateDefault(Default aDefault);

   void removeDefault(String value);
}
