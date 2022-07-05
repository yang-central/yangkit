package org.yangcentral.yangkit.model.api.stmt;

public interface Base extends YangBuiltinStatement, IdentifierRef {
   Identity getIdentity();
}
