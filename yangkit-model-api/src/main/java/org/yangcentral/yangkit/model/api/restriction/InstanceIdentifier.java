package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;

public interface InstanceIdentifier extends Restriction<YangAbsoluteLocationPath> {
   RequireInstance getRequireInstance();

   boolean isRequireInstance();

   RequireInstance getEffectiveRequireInstance();
}
