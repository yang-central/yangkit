package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Referencable {
   List<YangStatement> getReferencedBy();

   void addReference(YangStatement var1);

   void delReference(YangStatement var1);

   boolean isReferencedBy(YangStatement var1);
}
