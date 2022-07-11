package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.FractionDigits;
import org.yangcentral.yangkit.model.api.stmt.type.Range;

import java.math.BigDecimal;
/**
 * interface of decimal64 type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.3"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Decimal64 extends Restriction<BigDecimal> {
   /**
    * get fraction-digits, if no fraction-digits is defined, it will return null
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.FractionDigits
    * @author frank feng
    * @since 7/8/2022
    */
   FractionDigits getFractionDigits();
   /**
    * get effective fraction-digits, if no fraction-digits is defined, the effective fraction-digits of derived type will be returned.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.FractionDigits
    * @author frank feng
    * @since 7/8/2022
    */
   FractionDigits getEffectiveFractionDigits();
   /**
    * get range, if no range sub-statement is defined, it will return null.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Range
    * @author frank feng
    * @since 7/8/2022
    */
   Range getRange();
   /**
    * get effective range, if no range sub-statement is defined, the effective range of derived type will be returned.
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Range
    * @author frank feng
    * @since 7/8/2022
    */
   Range getEffectiveRange();

   ValidatorResult validate();

   BigDecimal getRangeMax();

   BigDecimal getRangeMin();
}
