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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

/**
 * CBOR codec for YANG container data.
 *
 * @author Yangkit Team
 */
public class ContainerDataCborCodec extends YangDataCborCodec<Container, ContainerData> {
    
    public ContainerDataCborCodec(Container schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected ObjectNode buildJson(ContainerData yangData) throws YangDataCborCodecException {
        // Serialize container children to JSON object
        return CborCodecUtil.serializeChildren(yangData);
    }
    
    @Override
    protected ContainerData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        ContainerDataImpl containerData = new ContainerDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        containerData.setQName(qName);
        
        // Process container children from JSON
        if (jsonNode != null && jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            
            // Iterate through fields and deserialize each child based on schema
            objectNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                
                try {
                    // Look up the schema node for this field
                    DataDefinition childSchema = 
                        getSchemaNode().getDataDefChild(fieldName);
                    
                    if (childSchema != null) {
                        // Deserialize based on the schema node type
                        if (childSchema instanceof Leaf) {
                            Leaf leafSchema = (Leaf) childSchema;
                            LeafDataCborCodec leafCodec = new LeafDataCborCodec(leafSchema);
                            LeafData<?> leafData = leafCodec.buildData(fieldValue, validatorResultBuilder);
                            containerData.addDataChild(leafData);
                        } else if (childSchema instanceof LeafList) {
                            LeafList leafListSchema = (LeafList) childSchema;
                            LeafListDataCborCodec leafListCodec = new LeafListDataCborCodec(leafListSchema);
                            LeafListData<?> leafListData = leafListCodec.buildData(fieldValue, validatorResultBuilder);
                            containerData.addDataChild(leafListData);
                        } else if (childSchema instanceof Container) {
                            Container containerSchema = (Container) childSchema;
                            ContainerDataCborCodec childContainerCodec = new ContainerDataCborCodec(containerSchema);
                            ContainerData childContainerData = childContainerCodec.buildData(fieldValue, validatorResultBuilder);
                            containerData.addDataChild(childContainerData);
                        }
                        // Add more types as needed (List, Choice, Case, etc.)
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize child field: " + fieldName, e);
                }
            });
        }
        
        return containerData;
    }
}
