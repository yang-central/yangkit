package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.Bit;

import java.util.List;
/**
 * the interface of bits type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.7"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Bits extends Restriction<List<String>> {
   long MAX_POSITION = 4294967295L;
   long MIN_POSITION = 0L;
   /**
    * get bits, if no bit sub-statement, it will return an empty list
    * @version 1.0.0
    * @throws
    * @return java.util.List<org.yangcentral.yangkit.model.api.stmt.type.Bit>
    * @author frank feng
    * @since 7/8/2022
    */
   List<Bit> getBits();
   /**
    * get effective bits, if no bit sub-statement, the effective bits of derived will be returned.
    * @version 1.0.0
    * @throws
    * @return java.util.List<org.yangcentral.yangkit.model.api.stmt.type.Bit>
    * @author frank feng
    * @since 7/8/2022
    */
   List<Bit> getEffectiveBits();
   /**
    * get specified bit's actual position (if no position sub-statement is defined,
    * it will return the value calculated according rule).
    * @param bitName bit's name
    * @version 1.0.0
    * @throws
    * @return java.lang.Long
    * @author frank feng
    * @since 7/8/2022
    */
   Long getBitActualPosition(String bitName);

   Long getMaxPosition();
}
