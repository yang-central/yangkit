package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.model.api.restriction.Restriction;

public interface Type extends YangBuiltinStatement, IdentifierRef {
   boolean isDerivedType();

   Typedef getDerived();

   Type getBuiltinType();

   Type getBaseType();

   Restriction getRestriction();
}
