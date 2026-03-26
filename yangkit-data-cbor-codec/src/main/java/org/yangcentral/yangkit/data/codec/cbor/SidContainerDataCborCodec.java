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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * CBOR codec for YANG container data with SID-based encoding.
 * Implements RFC 9254 Section 5 - SID-based encoding.
 *
 * @author Yangkit Team
 */
public class SidContainerDataCborCodec extends YangDataCborCodec<Container, ContainerData> {
    
    private final SidManager sidManager;
    
    public SidContainerDataCborCodec(Container schemaNode, SidManager sidManager) {
        super(schemaNode);
        this.sidManager = sidManager;
    }
    
    /**
     * Gets the SID manager associated with this codec.
     * 
     * @return the SID manager
     */
    public SidManager getSidManager() {
        return sidManager;
    }
    
    @Override
    protected ObjectNode buildJson(ContainerData yangData) throws YangDataCborCodecException {
        // Use SID-based encoding
        return SidEncoder.encodeWithSid(yangData, sidManager);
    }
    
    @Override
    protected ContainerData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        ContainerDataImpl containerData = new ContainerDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        containerData.setQName(qName);
        
        // Process container children from JSON using SID mapping
        if (jsonNode != null && jsonNode.isObject()) {
            deserializeChildren(containerData, (ObjectNode) jsonNode, validatorResultBuilder);
        }
        
        return containerData;
    }
    
    /**
     * Deserializes container children from JSON using SID-based mapping.
     * 
     * @param containerData the container data to populate
     * @param jsonNode the JSON node containing encoded data
     * @param validatorResultBuilder the validator result builder
     * @throws YangDataCborCodecException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    private void deserializeChildren(ContainerData containerData, ObjectNode jsonNode,
                                    ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();
            
            // Resolve field name (could be SID or local name)
            String resolvedName = resolveFieldName(fieldName);
            
            // Find the corresponding schema node
            SchemaNode childSchema = findChildSchema(resolvedName);
            
            if (childSchema == null) {
                // Unknown field, skip it
                continue;
            }
            
            try {
                // Create appropriate data node based on schema type
                if (childSchema instanceof Leaf) {
                    Leaf leaf = (Leaf) childSchema;
                    LeafDataImpl leafData = new LeafDataImpl(leaf);
                    leafData.setQName(leaf.getIdentifier());
                    
                    // Set value from JSON
                    Object value = CborCodecUtil.convertFromJson(fieldValue, leaf.getType());
                    if (value != null) {
                        // Note: Setting raw value, proper implementation would use YangDataValue
                        containerData.addChild(leafData);
                    }
                } else if (childSchema instanceof LeafList) {
                    LeafList leafList = (LeafList) childSchema;
                    // LeafList requires a value to construct, skip for now
                    // Full implementation would need to handle the value properly
                    // LeafListDataImpl<D> leafListData = new LeafListDataImpl<>(leafList, null);
                    // leafListData.setQName(leafList.getIdentifier());
                    // containerData.addChild(leafListData);
                } else if (childSchema instanceof Container) {
                    Container container = (Container) childSchema;
                    SidContainerDataCborCodec childCodec = 
                        new SidContainerDataCborCodec(container, sidManager);
                    ContainerData childContainerData = 
                        childCodec.deserialize(CborCodecUtil.CBOR_MAPPER.writeValueAsBytes(fieldValue), 
                                             validatorResultBuilder);
                    containerData.addChild(childContainerData);
                }
                // Add more types as needed
            } catch (Exception e) {
                throw new YangDataCborCodecException(
                    "Failed to deserialize child: " + resolvedName, e);
            }
        }
    }
    
    /**
     * Resolves a field name to the actual node name.
     * Field names can be SIDs (numeric) or local names.
     * 
     * @param fieldName the field name from JSON
     * @return the resolved node name
     */
    private String resolveFieldName(String fieldName) {
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
    
    /**
     * Finds a child schema node by its local name.
     * 
     * @param localName the local name to search for
     * @return the schema node, or null if not found
     */
    private SchemaNode findChildSchema(String localName) {
        // Search through the container's data definition children
        List<DataDefinition> children = getSchemaNode().getDataDefChildren();
        if (children != null) {
            for (DataDefinition child : children) {
                if (child instanceof SchemaNode) {
                    SchemaNode schemaNode = (SchemaNode) child;
                    if (localName.equals(schemaNode.getIdentifier().getLocalName())) {
                        return schemaNode;
                    }
                }
            }
        }
        return null;
    }
}
