package org.yangcentral.yangkit.model.api.stmt;

public interface MaxElements extends YangBuiltinStatement {
   boolean isUnbounded();

   Integer getValue();
}
