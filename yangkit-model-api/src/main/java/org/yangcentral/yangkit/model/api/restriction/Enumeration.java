package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;

import java.util.List;
/**
 * the definition of enumeration type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.6"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Enumeration extends Restriction<String> {
   int MAX_VALUE = Integer.MAX_VALUE;
   int MIN_VALUE = Integer.MIN_VALUE;
   /**
    * get specified enum's actual value, if no value sub-statement is defined, it will return the value calculated according rule
    * @param enumName  the name of enum
    * @version 1.0.0
    * @throws
    * @return java.lang.Integer
    * @author frank feng
    * @since 7/8/2022
    */
   Integer getEnumActualValue(String enumName);

   Integer getHighestValue();
   /**
    * get enums, if no enum sub-statement is defined, it will return empty list.
    * @version 1.0.0
    * @throws
    * @return java.util.List<org.yangcentral.yangkit.model.api.stmt.type.YangEnum>
    * @author frank feng
    * @since 7/8/2022
    */
   List<YangEnum> getEnums();
   /**
    * get effective enums, if no enum sub-statement is defined, it will return the effective enums of derived type
    * @version 1.0.0
    * @throws
    * @return java.util.List<org.yangcentral.yangkit.model.api.stmt.type.YangEnum>
    * @author frank feng
    * @since 7/8/2022
    */
   List<YangEnum> getEffectiveEnums();
}
