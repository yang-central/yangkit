package org.yangcentral.yangkit.model.api.stmt;

public interface MultiInstancesDataNode {
   MinElements getMinElements();

   void setMinElements(MinElements minElements);

   MaxElements getMaxElements();

   void setMaxElements(MaxElements maxElements);

   OrderedBy getOrderedBy();

   boolean isDataArray();

   void setDataIsArray();

}
