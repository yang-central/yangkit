package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.Identifiable;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;
/**
 * the interface of bit statement.
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.7.4"/>
 * @version 1.0.0
 * @throws
 * @return
 * @author frank feng
 * @since 7/8/2022
 */
public interface Bit extends Entity, Identifiable, IfFeatureSupport {
   Position getPosition();
}
