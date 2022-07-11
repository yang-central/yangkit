package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Range;
/**
 * the super interface for all integer type(int8/int16/int32/int64/uint8/uint16/uint32/uint64)
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.2"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangInteger<T extends Comparable> extends Restriction<T> {
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

   ValidatorResult setRange(Range var1);

   T getMax();

   T getMin();
}
