package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.QName;
import java.net.URI;
/**
 * define global constant for YANG
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class Yang {
   public static final Namespace NAMESPACE = new Namespace(URI.create("urn:ietf:params:xml:ns:yang:1"), "yang");
   public static final String VERSION_1 = "1";
   public static final String VERSION_11 = "1.1";
   public static final String YANG_SUFFIX = ".yang";
   public static final String YIN_SUFFIX = ".yin";
   public static final QName UNKNOWN;
   public static final String MAX = "max";
   public static final String MIN = "min";

   static {
      UNKNOWN = new QName(NAMESPACE, "unknown");
   }
}
