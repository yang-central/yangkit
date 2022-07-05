package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Base;

import java.util.List;

public interface IdentityRef extends Restriction<QName> {
   List<Base> getBases();

   List<Base> getEffectiveBases();
}
