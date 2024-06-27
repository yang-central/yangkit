package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.Typedef;
/**
 * the universal interface for all restriction(e.g. int8,string,leafref,etc.)
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Restriction<T> {
   /**
    * get derived typedef,if it's builtin type,return null.
    * @version 1.0.0
    * @return org.yangcentral.yangkit.model.api.stmt.Typedef
    * @author frank feng
    * @since 7/8/2022
    */
   Typedef getDerived();
   /**
    * evaluate the value(judge whether the value matches the restriction)
    * @param value the value for this restriction
    * @version 1.0.0
    * @return boolean true:the value matches the restriction, false:the value doesn't match the restriction.
    * @author frank feng
    * @since 7/8/2022
    */
   boolean evaluate(T value);
}
