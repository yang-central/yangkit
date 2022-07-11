package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
/**
 * the interface of instance-identifier
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.13"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface InstanceIdentifier extends Restriction<YangAbsoluteLocationPath> {
   /**
    * get require-instance, if no require-instance sub-statement is defined, it will return null
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.RequireInstance
    * @author frank feng
    * @since 7/8/2022
    */
   RequireInstance getRequireInstance();
   /**
    * judge whether it's require-instance
    * @version 1.0.0
    * @throws
    * @return boolean if require-instance is defined,return it's value, else return the require-instance's value of derived type,
    * if no derived-type, return true (default value).
    * @author frank feng
    * @since 7/8/2022
    */
   boolean isRequireInstance();
   /**
    * get effective require-instance, if no require-instance is defined, if it has derived type, return derived type's effective
    * require-instance, otherwise, it will return require-instance true.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.RequireInstance
    * @author frank feng
    * @since 7/8/2022
    */
   RequireInstance getEffectiveRequireInstance();
}
