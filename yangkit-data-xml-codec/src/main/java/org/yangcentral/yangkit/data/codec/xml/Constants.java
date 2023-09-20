package org.yangcentral.yangkit.data.codec.xml;

import org.dom4j.QName;


public class Constants {
    /**
     * netconf default prefix
     */
    public static final String NETCONF_NS_PREFIX = "nc";

    /**
     * netconf namespace
     */
    public static final String NETCONF_NS_URI = "urn:ietf:params:xml:ns:netconf:base:1.0";

    public static final QName OPERATION =
            QName.get("operation", Constants.NETCONF_NS_PREFIX, Constants.NETCONF_NS_URI);
}
