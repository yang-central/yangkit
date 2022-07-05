package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.ErrorReporter;
import org.yangcentral.yangkit.model.api.stmt.MetaDef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

public interface Pattern extends YangBuiltinStatement, MetaDef, ErrorReporter {
   java.util.regex.Pattern getPattern();

   Modifier getModifier();
}
