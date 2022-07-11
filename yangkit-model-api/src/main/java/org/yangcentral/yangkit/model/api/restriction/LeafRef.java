package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
/**
 * interface of leafref type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.9"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface LeafRef extends Restriction<Object> {
   /**
    * get reference path, if no path sub-statement is defined, it will return null
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Path
    * @author frank feng
    * @since 7/8/2022
    */
   Path getPath();
   /**
    * get effective reference path, if no path sub-statement is defined, it will return the effective reference path of derived type.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Path
    * @author frank feng
    * @since 7/8/2022
    */
   Path getEffectivePath();
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
    * get effective require-instance, if no require-instance is defined, if it has derived type, return derived type's effective
    * require-instance, otherwise, it will return require-instance true.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.RequireInstance
    * @author frank feng
    * @since 7/8/2022
    */
   RequireInstance getEffectiveRequireInstance();
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

   TypedDataNode getReferencedNode();

   void setReferencedNode(TypedDataNode var1);
}
