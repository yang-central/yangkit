package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Base;

import java.util.List;
/**
 * the interface of identityref type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.10">identity-ref</a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface IdentityRef extends Restriction<QName> {
   /**
    * get bases, if no base sub-statement is defined, it will return empty list
    * @version 1.0.0
    * @return list of base
    * @author frank feng
    * @since 7/8/2022
    */
   List<Base> getBases();
   /**
    * get effective bases, if no base sub-statement is defined, it will return the effective bases of derived type.
    * @version 1.0.0
    * @return list of base
    * @author frank feng
    * @since 7/8/2022
    */
   List<Base> getEffectiveBases();
}
