package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.Type;

import java.util.List;
/**
 * the interface of union type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.12"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Union extends Restriction<Object> {
   List<Type> getTypes();
   List<Type> getActualTypes();
}
