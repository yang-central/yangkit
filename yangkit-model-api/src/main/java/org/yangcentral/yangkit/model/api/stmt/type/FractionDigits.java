package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
/**
 * interface for fraction-digits statement
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.3.4"></a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface FractionDigits extends YangBuiltinStatement {
   Integer getValue();
}
