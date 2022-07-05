package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;

public interface YangEnum extends Entity, IfFeatureSupport {
   Value getValue();
}
