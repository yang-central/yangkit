package org.yangcentral.yangkit.data.impl.util;

import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.OrderBy;
import org.yangcentral.yangkit.model.api.stmt.OrderedBy;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NetconfEditUtil {
    public static final String NETCONF_NS_URI = "urn:ietf:params:xml:ns:netconf:base:1.0";

    public static final QName OPERATION_QNAME = new QName(NETCONF_NS_URI, "operation");
    public static final QName INSERT_QNAME = new QName(NETCONF_NS_URI, "insert");
    public static final QName KEY_QNAME = new QName(NETCONF_NS_URI, "key");
    public static final QName VALUE_QNAME = new QName(NETCONF_NS_URI, "value");
    public static final QName SELECT_QNAME = new QName(NETCONF_NS_URI, "select");

    private static final Pattern BRACKETED_KEY_PATTERN =
            Pattern.compile("\\[\\s*([^=\\]\\s]+)\\s*=\\s*(['\"])(.*?)\\2\\s*\\]");

    private NetconfEditUtil() {
    }

    public static Attribute getAttribute(YangData<?> yangData, QName qName) {
        if (yangData == null || qName == null) {
            return null;
        }
        Attribute attribute = yangData.getAttribute(qName);
        if (attribute != null) {
            return attribute;
        }
        List<Attribute> sameNameAttributes = yangData.getAttributes(qName.getLocalName());
        if (sameNameAttributes == null) {
            return null;
        }
        for (Attribute candidate : sameNameAttributes) {
            if (candidate == null) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    public static String getAttributeValue(YangData<?> yangData, QName qName) {
        Attribute attribute = getAttribute(yangData, qName);
        if (attribute == null || attribute.getValue() == null) {
            return null;
        }
        String value = attribute.getValue().trim();
        return value.isEmpty() ? null : value;
    }

    public static String getOperation(YangData<?> yangData) {
        return getAttributeValue(yangData, OPERATION_QNAME);
    }

    public static String getInsert(YangData<?> yangData) {
        return getAttributeValue(yangData, INSERT_QNAME);
    }

    public static String getKey(YangData<?> yangData) {
        return getAttributeValue(yangData, KEY_QNAME);
    }

    public static String getValue(YangData<?> yangData) {
        return getAttributeValue(yangData, VALUE_QNAME);
    }

    public static String getSelect(YangData<?> yangData) {
        return getAttributeValue(yangData, SELECT_QNAME);
    }

    public static boolean isEditAttribute(Attribute attribute) {
        if (attribute == null || attribute.getName() == null) {
            return false;
        }
        String localName = attribute.getName().getLocalName();
        return "operation".equals(localName)
                || "insert".equals(localName)
                || "key".equals(localName)
                || "value".equals(localName)
                || "select".equals(localName);
    }

    public static boolean isUserOrdered(YangData<?> child) {
        if (child == null) {
            return false;
        }
        SchemaNode schemaNode = child.getSchemaNode();
        if (!(schemaNode instanceof MultiInstancesDataNode)) {
            return false;
        }
        OrderedBy orderedBy = ((MultiInstancesDataNode) schemaNode).getOrderedBy();
        return orderedBy != null && orderedBy.getOrderedBy() == OrderBy.USER;
    }

    public static Map<String, String> parseKeyPredicate(String keyPredicate) {
        if (keyPredicate == null) {
            return Collections.emptyMap();
        }
        String normalized = keyPredicate.trim();
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();
        Matcher matcher = BRACKETED_KEY_PATTERN.matcher(normalized);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(3));
        }
        if (!result.isEmpty()) {
            return result;
        }

        String[] pairs = normalized.split("\\s*,\\s*");
        for (String pair : pairs) {
            if (pair == null) {
                continue;
            }
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            result.put(keyValue[0].trim(), unquote(keyValue[1].trim()));
        }
        return result;
    }

    public static boolean matchesListReference(ListData listData, String keyPredicate) {
        Map<String, String> expectedKeys = parseKeyPredicate(keyPredicate);
        if (listData == null || expectedKeys.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : expectedKeys.entrySet()) {
            boolean matched = false;
            for (LeafData key : listData.getKeys()) {
                if (key == null || key.getQName() == null) {
                    continue;
                }
                String candidateName = key.getQName().getLocalName();
                String qualifiedName = key.getQName().getQualifiedName();
                if (!entry.getKey().equals(candidateName) && !entry.getKey().equals(qualifiedName)) {
                    continue;
                }
                if (entry.getValue().equals(key.getStringValue())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesLeafListReference(YangData<?> yangData, String value) {
        if (!(yangData instanceof TypedData) || value == null) {
            return false;
        }
        String candidateValue = ((TypedData<?, ?>) yangData).getStringValue();
        return value.equals(candidateValue);
    }

    private static String unquote(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
