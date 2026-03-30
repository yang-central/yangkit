/*
 * Copyright 2023 Yangkit Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yangcentral.yangkit.data.codec.cbor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for CBOR codec operations.
 * Provides helper methods for converting between YANG data and CBOR/JSON structures.
 *
 * @author Yangkit Team
 */
public class CborCodecUtil {

    static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static final ObjectMapper CBOR_MAPPER = new ObjectMapper(new com.fasterxml.jackson.dataformat.cbor.CBORFactory());

    /**
     * Converts a YANG data value to a JSON node suitable for CBOR encoding.
     *
     * <p>Type mapping (RFC 9254):
     * <ul>
     *   <li>null / empty → NullNode (CBOR null 0xF6)</li>
     *   <li>byte[] / binary → BinaryNode (CBOR byte string, major type 2)</li>
     *   <li>Boolean → BooleanNode (CBOR true/false)</li>
     *   <li>BigDecimal / decimal64 → TextNode (string representation to preserve precision;
     *       RFC 9254 prefers tag 4, deferred as known limitation)</li>
     *   <li>List&lt;String&gt; / bits → TextNode (space-separated active bit names)</li>
     *   <li>Integer/Long → NumberNode (CBOR signed/unsigned integer)</li>
     *   <li>String → TextNode (CBOR text string)</li>
     * </ul>
     *
     * @param value the data value
     * @return JSON node representation
     */
    public static JsonNode convertToJson(YangDataValue<?, ?> value) {
        if (value == null) {
            return JSON_MAPPER.nullNode();
        }

        Object val;
        try {
            val = value.getValue();
        } catch (YangCodecException e) {
            // Fall back to string representation
            try {
                String strVal = value.getStringValue();
                return strVal != null ? JSON_MAPPER.getNodeFactory().textNode(strVal)
                                     : JSON_MAPPER.nullNode();
            } catch (YangCodecException ex) {
                return JSON_MAPPER.nullNode();
            }
        }

        return toJsonNode(val);
    }

    /**
     * Converts a raw Java value to a JsonNode, applying RFC 9254 type rules.
     */
    static JsonNode toJsonNode(Object val) {
        if (val == null) {
            return JSON_MAPPER.nullNode();
        }
        // empty type (apache commons ObjectUtils.Null or similar sentinel)
        if (val.getClass().getSimpleName().equals("Null")) {
            return JSON_MAPPER.nullNode();
        }
        // binary → CBOR byte string (major type 2) via BinaryNode
        if (val instanceof byte[]) {
            return JSON_MAPPER.getNodeFactory().binaryNode((byte[]) val);
        }
        if (val instanceof Boolean) {
            return JSON_MAPPER.getNodeFactory().booleanNode((Boolean) val);
        }
        // decimal64 → text string (preserves precision; CBOR tag 4 is a known limitation)
        if (val instanceof BigDecimal) {
            return JSON_MAPPER.getNodeFactory().textNode(val.toString());
        }
        if (val instanceof Integer) {
            return JSON_MAPPER.getNodeFactory().numberNode((Integer) val);
        }
        if (val instanceof Long) {
            return JSON_MAPPER.getNodeFactory().numberNode((Long) val);
        }
        if (val instanceof Float) {
            return JSON_MAPPER.getNodeFactory().numberNode((Float) val);
        }
        if (val instanceof Double) {
            return JSON_MAPPER.getNodeFactory().numberNode((Double) val);
        }
        if (val instanceof Number) {
            return JSON_MAPPER.getNodeFactory().numberNode(((Number) val).longValue());
        }
        // bits → space-separated text string of active bit names
        if (val instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (Object bit : (List<?>) val) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(bit.toString());
            }
            return JSON_MAPPER.getNodeFactory().textNode(sb.toString());
        }
        // Default: use string representation
        return JSON_MAPPER.getNodeFactory().textNode(val.toString());
    }

    /**
     * Converts a JSON node to a string suitable for passing to YangDataBuilderFactory.
     * Returns null for null/empty nodes.
     *
     * @param jsonNode the JSON node from CBOR deserialization
     * @return string value for builder, or null for empty/null types
     */
    public static String toYangText(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        if (jsonNode.isBinary()) {
            // Re-encode binary to base64 for the builder
            try {
                byte[] bytes = jsonNode.binaryValue();
                return java.util.Base64.getEncoder().encodeToString(bytes);
            } catch (java.io.IOException e) {
                return jsonNode.asText();
            }
        }
        return jsonNode.asText();
    }

    /**
     * Serializes the data children of a YANG container to a JSON object.
     *
     * <p>Handles grouping: multiple {@link ListData} entries with the same QName are
     * collected into a single JSON array, and multiple {@link LeafListData} entries
     * with the same QName are similarly collected into a JSON array.
     *
     * @param container the YANG container (ContainerData or ListData entry)
     * @return JSON object with all serialized children
     * @throws YangDataCborCodecException if serialization fails
     */
    public static ObjectNode serializeChildren(YangDataContainer container)
            throws YangDataCborCodecException {
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        if (container == null) {
            return rootNode;
        }

        List<YangData<?>> children;
        try {
            children = container.getDataChildren();
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to get data children", e);
        }

        // Group children by local name to handle list/leaf-list multi-instances
        Map<String, List<YangData<?>>> groups = new LinkedHashMap<>();
        for (YangData<?> child : children) {
            if (child == null) continue;
            String key = child.getQName().getLocalName();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(child);
        }

        for (Map.Entry<String, List<YangData<?>>> entry : groups.entrySet()) {
            String fieldName = entry.getKey();
            List<YangData<?>> group = entry.getValue();
            YangData<?> first = group.get(0);

            try {
                if (first instanceof LeafListData) {
                    // leaf-list: array of scalar values
                    ArrayNode arrayNode = JSON_MAPPER.createArrayNode();
                    for (YangData<?> item : group) {
                        YangDataValue<?, ?> v = ((LeafListData<?>) item).getValue();
                        arrayNode.add(v != null ? convertToJson(v) : JSON_MAPPER.nullNode());
                    }
                    rootNode.set(fieldName, arrayNode);

                } else if (first instanceof ListData) {
                    // list: array of entry objects
                    ArrayNode arrayNode = JSON_MAPPER.createArrayNode();
                    for (YangData<?> item : group) {
                        arrayNode.add(serializeListEntry((ListData) item));
                    }
                    rootNode.set(fieldName, arrayNode);

                } else if (first instanceof LeafData) {
                    YangDataValue<?, ?> v = ((LeafData<?>) first).getValue();
                    if (v != null) {
                        rootNode.set(fieldName, convertToJson(v));
                    }

                } else if (first instanceof ContainerData) {
                    rootNode.set(fieldName, serializeChildren((ContainerData) first));

                } else {
                    // Fallback: try to serialize as a container
                    if (first instanceof YangDataContainer) {
                        rootNode.set(fieldName, serializeChildren((YangDataContainer) first));
                    }
                }
            } catch (YangDataCborCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new YangDataCborCodecException("Failed to serialize child: " + fieldName, e);
            }
        }
        return rootNode;
    }

    /**
     * Serializes a single list entry (ListData) as a JSON object containing
     * all of its data children (keys + non-key fields).
     *
     * <p>Key leaves are stored separately in {@link ListData#getKeys()} and are
     * NOT returned by {@link YangDataContainer#getDataChildren()}, so they must
     * be serialized explicitly.
     */
    static ObjectNode serializeListEntry(ListData entry) throws YangDataCborCodecException {
        ObjectNode node = serializeChildren(entry);
        // Include key leaves which are NOT part of getDataChildren()
        List<LeafData> keys = entry.getKeys();
        if (keys != null) {
            for (LeafData<?> key : keys) {
                if (key == null) continue;
                String fieldName = key.getQName().getLocalName();
                YangDataValue<?, ?> v = key.getValue();
                if (v != null) {
                    node.set(fieldName, convertToJson(v));
                }
            }
        }
        return node;
    }
}
