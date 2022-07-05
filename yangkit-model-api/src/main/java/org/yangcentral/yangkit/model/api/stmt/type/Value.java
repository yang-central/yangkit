package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

public interface Value extends YangBuiltinStatement {
   int getValue();
}
