package org.yangcentral.yangkit.model.api.stmt;

public interface Extension extends Entity, Identifiable,YangBuiltinStatement {
   Argument getArgument();
}
