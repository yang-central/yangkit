package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface DataDefContainer {
   List<DataDefinition> getDataDefChildren();

   DataDefinition getDataDefChild(String var1);

   ValidatorResult addDataDefChild(DataDefinition var1);
}
