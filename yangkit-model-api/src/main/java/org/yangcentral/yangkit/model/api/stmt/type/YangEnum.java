package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

public interface YangEnum extends Entity, IfFeatureSupport, YangBuiltinStatement {
   Value getValue();
}
