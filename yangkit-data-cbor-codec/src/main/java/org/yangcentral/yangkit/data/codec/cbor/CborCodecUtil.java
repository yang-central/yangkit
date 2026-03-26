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
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.math.BigDecimal;
import java.util.List;

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
     * Converts a YANG data value to a JSON node.
     *
     * @param value the data value
     * @return JSON node representation
     */
    public static JsonNode convertToJson(YangDataValue<?,?> value) {
        if (value == null) {
            return JSON_MAPPER.nullNode();
        }
        
        Object val;
        try {
            val = value.getValue();
        } catch (YangCodecException e) {
            // If we can't get the value, return null node
            return JSON_MAPPER.nullNode();
        }
        
        if (val == null) {
            return JSON_MAPPER.nullNode();
        }
        
        // Handle different types according to RFC 9254
        if (val instanceof String) {
            return JSON_MAPPER.getNodeFactory().textNode((String) val);
        } else if (val instanceof Integer || val instanceof Long) {
            return JSON_MAPPER.getNodeFactory().numberNode(((Number) val).longValue());
        } else if (val instanceof BigDecimal) {
            return JSON_MAPPER.getNodeFactory().numberNode((BigDecimal) val);
        } else if (val instanceof Boolean) {
            return JSON_MAPPER.getNodeFactory().booleanNode((Boolean) val);
        } else if (val instanceof Number) {
            // Handle other number types by converting to string first, then parsing
            try {
                if (val instanceof Float || val instanceof Double) {
                    return JSON_MAPPER.getNodeFactory().numberNode(((Number) val).doubleValue());
                } else {
                    return JSON_MAPPER.getNodeFactory().numberNode(((Number) val).longValue());
                }
            } catch (Exception e) {
                return JSON_MAPPER.getNodeFactory().textNode(val.toString());
            }
        } else {
            // Default: convert to string
            return JSON_MAPPER.getNodeFactory().textNode(val.toString());
        }
    }
    
    /**
     * Converts a JSON node to a YANG data value.
     * This is a simplified implementation that returns the raw value.
     *
     * @param jsonNode the JSON node
     * @param leafType the leaf type (not used in this simplified version)
     * @return the converted value object
     */
    @SuppressWarnings("unchecked")
    public static <D> Object convertFromJson(JsonNode jsonNode, Object leafType) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        
        // Convert based on JSON value type
        if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isLong()) {
            return jsonNode.asLong();
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isBigDecimal()) {
            return jsonNode.decimalValue();
        } else if (jsonNode.isNumber()) {
            return jsonNode.numberValue();
        } else if (jsonNode.isArray()) {
            // For arrays, return as-is for now
            return jsonNode;
        } else if (jsonNode.isObject()) {
            // For objects, return as-is for now
            return jsonNode;
        } else {
            return jsonNode.asText();
        }
    }
    
    /**
     * Serializes children of a YANG container to a JSON object.
     *
     * @param container the YANG container
     * @return JSON object containing serialized children
     * @throws YangDataCborCodecException if serialization fails
     */
    public static ObjectNode serializeChildren(YangDataContainer container) 
            throws YangDataCborCodecException {
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        
        if (container == null) {
            return rootNode;
        }
        
        try {
            // Serialize all data children
            List<YangData<?>> children = container.getDataChildren();
            for (YangData<?> child : children) {
                serializeDataChild(rootNode, child);
            }
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to serialize container children", e);
        }
        
        return rootNode;
    }
    
    /**
     * Serializes a single data child to the parent JSON object.
     *
     * @param parentNode the parent JSON object
     * @param child the YANG data child
     * @throws YangDataCborCodecException if serialization fails
     */
    @SuppressWarnings("unchecked")
    private static void serializeDataChild(ObjectNode parentNode, YangData<?> child) 
            throws YangDataCborCodecException {
        if (child == null) {
            return;
        }
        
        QName qName = child.getQName();
        String fieldName = qName.getLocalName();
        
        try {
            if (child instanceof LeafData) {
                LeafData<?> leafData = (LeafData<?>) child;
                YangDataValue<?,?> value = leafData.getValue();
                if (value != null) {
                    parentNode.set(fieldName, convertToJson(value));
                }
            } else if (child instanceof LeafListData) {
                LeafListData<?> leafListData = (LeafListData<?>) child;
                ArrayNode arrayNode = parentNode.putArray(fieldName);
                YangDataValue<?,?> value = leafListData.getValue();
                if (value != null) {
                    arrayNode.add(convertToJson(value));
                }
            } else if (child instanceof ContainerData) {
                ContainerData containerData = (ContainerData) child;
                ObjectNode childNode = serializeChildren(containerData);
                parentNode.set(fieldName, childNode);
            } else if (child instanceof ListData) {
                ListData listData = (ListData) child;
                ArrayNode arrayNode = parentNode.putArray(fieldName);
                // Serialize list entries - simplified implementation
                // Full implementation would iterate through actual entries
            }
            // Add more types as needed
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to serialize data child: " + fieldName, e);
        }
    }
}
