package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Length;
import org.yangcentral.yangkit.model.api.stmt.type.Pattern;

import java.math.BigInteger;
import java.util.List;
/**
 * interface for string type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.4">string</a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangString extends Restriction<String> {
   BigInteger MAX_LENGTH = new BigInteger("18446744073709551615");
   BigInteger MIN_LENGTH = BigInteger.ZERO;
   /**
    * get length information, if binary type has no length sub-statement,it will return null
    * @version 1.0.0
    * @return org.yangcentral.yangkit.model.api.stmt.type.Length
    * @author frank feng
    * @since 7/8/2022
    */
   Length getLength();
   /**
    * get effective length info. if no length sub-statement is defined, it will return the length info of the type
    * which is derived from , and if it has no derived type, it will return 0..max
    * @version 1.0.0
    * @return org.yangcentral.yangkit.model.api.stmt.type.Length
    * @author frank feng
    * @since 7/8/2022
    */
   Length getEffectiveLength();
   /**
    * get patterns, if no pattern sub-statement is defined, it will return empty list.
    * @version 1.0.0
    * @return list of pattern
    * @author frank feng
    * @since 7/8/2022
    */
   List<Pattern> getPatterns();
   /**
    * get effective patterns, if no pattern sub-statement is defined, it will return the effective patterns of derived type,
      if no derived type, the empty list will be returned.
    * @version 1.0.0
    * @return list of pattern
    * @author frank feng
    * @since 7/8/2022
    */
   List<Pattern> getEffectivePatterns();

   BigInteger getMaxLength();

   BigInteger getMinLength();

   ValidatorResult setLength(Length var1);
}
