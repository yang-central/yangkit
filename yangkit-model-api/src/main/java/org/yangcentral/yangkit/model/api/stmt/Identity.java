package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Identity extends Entity, IfFeatureSupport, Identifiable,YangBuiltinStatement {
   List<Base> getBases();

   Base getBase(String name);

   boolean isDerived(Identity other);

   boolean isDerivedOrSelf(Identity other);
}
