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
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;

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
            // Deserialize children from the JSON object
            // Implementation will iterate through fields and create appropriate data nodes
            jsonNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                // TODO: Implement child deserialization logic based on schema
                // This requires looking up the schema node for each field
            });
        }
        
        return containerData;
    }
}
