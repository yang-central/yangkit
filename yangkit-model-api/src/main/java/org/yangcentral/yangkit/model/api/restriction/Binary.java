package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Length;

import java.math.BigInteger;
/**
 * the definition of binary type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.8"/>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Binary extends Restriction<byte[]> {
   /**
    * get length information, if binary type has no length sub-statement,it will return null
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Length
    * @author frank feng
    * @since 7/8/2022
    */
   Length getLength();
   /**
    * get effective length info. if no length sub-statement is defined, it will return the length info of the type
    * which is derived from , and if it has no derived type, it will return 0..max
    * @version 1.0.0
    * @throws
    * @return org.yangcentral.yangkit.model.api.stmt.type.Length
    * @author frank feng
    * @since 7/8/2022
    */
   Length getEffectiveLength();

   ValidatorResult setLength(Length var1);

   BigInteger getMaxLength();

   BigInteger getMinLength();
}
