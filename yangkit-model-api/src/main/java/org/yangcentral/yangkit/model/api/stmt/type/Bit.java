package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.Identifiable;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

/**
 * the interface of bit statement.
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.7.4">bit</a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Bit extends Entity, Identifiable, IfFeatureSupport, YangBuiltinStatement {
   Position getPosition();
}
