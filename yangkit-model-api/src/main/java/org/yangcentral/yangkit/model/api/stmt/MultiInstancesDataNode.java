package org.yangcentral.yangkit.model.api.stmt;

public interface MultiInstancesDataNode {
   MinElements getMinElements();

   void setMinElements(MinElements var1);

   MaxElements getMaxElements();

   void setMaxElements(MaxElements var1);

   OrderedBy getOrderedBy();
}
