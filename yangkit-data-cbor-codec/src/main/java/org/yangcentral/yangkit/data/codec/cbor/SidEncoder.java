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

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;

/**
 * SID encoding utilities for CBOR codec.
 * Provides helper methods for SID-based encoding as per RFC 9254 Section 5.
 * 
 * @author Yangkit Team
 */
public class SidEncoder {
    
    /**
     * CBOR tag for SID-based encoding
     */
    public static final int CBOR_TAG_SID = 60000;
    
    /**
     * Encodes a container's children using SID-based encoding.
     * 
     * @param container the YANG container data
     * @param sidManager the SID manager
     * @return JSON node with SIDs as field names
     * @throws YangDataCborCodecException if encoding fails
     */
    public static ObjectNode encodeWithSid(YangDataContainer container, SidManager sidManager) 
            throws YangDataCborCodecException {
        
        ObjectNode rootNode = CborCodecUtil.JSON_MAPPER.createObjectNode();
        
        if (container == null) {
            return rootNode;
        }
        
        try {
            // Encode all data children using SIDs
            for (org.yangcentral.yangkit.data.api.model.YangData<?> child : 
                    container.getDataChildren()) {
                encodeChild(rootNode, child, sidManager);
            }
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to encode children with SID", e);
        }
        
        return rootNode;
    }
    
    /**
     * Encodes a single child element using SID.
     * 
     * @param parentNode the parent node
     * @param child the child data
     * @param sidManager the SID manager
     * @throws YangDataCborCodecException if encoding fails
     */
    @SuppressWarnings("unchecked")
    private static void encodeChild(ObjectNode parentNode, 
                                   org.yangcentral.yangkit.data.api.model.YangData<?> child,
                                   SidManager sidManager) 
            throws YangDataCborCodecException {
        
        if (child == null) {
            return;
        }
        
        QName qName = child.getQName();
        
        // Get SID for this node
        Long sid = sidManager.getSid(qName);
        
        // Use SID as field name if available
        String fieldName = (sid != null) ? String.valueOf(sid) : qName.getLocalName();
        
        try {
            if (child instanceof org.yangcentral.yangkit.data.api.model.LeafData) {
                org.yangcentral.yangkit.data.api.model.LeafData<?> leafData = 
                    (org.yangcentral.yangkit.data.api.model.LeafData<?>) child;
                Object value = null;
                try {
                    value = leafData.getValue().getValue();
                } catch (Exception ex) {
                    // Ignore codec exceptions
                }
                if (value != null) {
                    parentNode.set(fieldName, CborCodecUtil.convertToJson(leafData.getValue()));
                }
            } else if (child instanceof org.yangcentral.yangkit.data.api.model.LeafListData) {
                org.yangcentral.yangkit.data.api.model.LeafListData<?> leafListData = 
                    (org.yangcentral.yangkit.data.api.model.LeafListData<?>) child;
                ArrayNode arrayNode = parentNode.putArray(fieldName);
                Object value = null;
                try {
                    value = leafListData.getValue().getValue();
                } catch (Exception ex) {
                    // Ignore
                }
                if (value != null) {
                    arrayNode.add(CborCodecUtil.convertToJson(leafListData.getValue()));
                }
            } else if (child instanceof org.yangcentral.yangkit.data.api.model.ContainerData) {
                org.yangcentral.yangkit.data.api.model.ContainerData containerData = 
                    (org.yangcentral.yangkit.data.api.model.ContainerData) child;
                // Recursively encode nested containers with SID
                ObjectNode childNode = encodeWithSid(containerData, sidManager);
                parentNode.set(fieldName, childNode);
            } else if (child instanceof org.yangcentral.yangkit.data.api.model.ListData) {
                org.yangcentral.yangkit.data.api.model.ListData listData = 
                    (org.yangcentral.yangkit.data.api.model.ListData) child;
                ArrayNode arrayNode = parentNode.putArray(fieldName);
                // Encode list entries with SID
                // Note: ListData stores entries in its parent container, we just encode the structure here
                // The actual entries would be handled by the container's data children iteration
            }
            // Add more types as needed
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to encode child: " + qName, e);
        }
    }
    
    /**
     * Decodes a JSON node using SID-based mapping.
     * 
     * @param jsonNode the JSON node with SID field names
     * @param sidManager the SID manager
     * @return decoded object node with original names (for compatibility)
     */
    public static ObjectNode decodeWithSid(JsonNode jsonNode, SidManager sidManager) {
        ObjectNode result = CborCodecUtil.JSON_MAPPER.createObjectNode();
        
        if (jsonNode == null || !jsonNode.isObject()) {
            return result;
        }
        
        java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = 
            ((ObjectNode) jsonNode).fields();
        
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode valueNode = entry.getValue();
            
            // Try to resolve field name as SID
            String resolvedName = resolveFieldName(fieldName, sidManager);
            result.set(resolvedName, valueNode);
        }
        
        return result;
    }
    
    /**
     * Resolves a field name (SID or local name) to the actual node name.
     * 
     * @param fieldName the field name
     * @param sidManager the SID manager
     * @return the resolved name
     */
    private static String resolveFieldName(String fieldName, SidManager sidManager) {
        // Try to parse as SID
        try {
            Long sid = Long.parseLong(fieldName);
            QName qName = sidManager.getQName(sid);
            if (qName != null) {
                return qName.getLocalName();
            }
        } catch (NumberFormatException e) {
            // Not a SID, use as-is
        }
        
        return fieldName;
    }
}
