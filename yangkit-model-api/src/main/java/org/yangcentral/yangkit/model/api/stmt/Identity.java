package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Identity extends Entity, IfFeatureSupport, Identifiable {
   List<Base> getBases();

   Base getBase(String var1);

   boolean isDerived(Identity var1);

   boolean isDerivedOrSelf(Identity var1);
}
