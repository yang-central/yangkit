package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Referencable {
   List<YangStatement> getReferencedBy();

   void addReference(YangStatement yangStatement);

   void delReference(YangStatement yangStatement);

   boolean isReferencedBy(YangStatement yangStatement);
}
