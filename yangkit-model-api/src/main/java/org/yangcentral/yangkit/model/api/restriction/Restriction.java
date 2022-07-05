package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.Typedef;

public interface Restriction<T> {
   Typedef getDerived();

   boolean evaluated(T var1);
}
