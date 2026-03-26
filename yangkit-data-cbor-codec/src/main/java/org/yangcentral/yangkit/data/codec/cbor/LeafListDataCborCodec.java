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
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

/**
 * CBOR codec for YANG leaf-list data.
 *
 * @author Yangkit Team
 */
public class LeafListDataCborCodec extends YangDataCborCodec<LeafList, LeafListData<?>> {
    
    public LeafListDataCborCodec(LeafList schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected ArrayNode buildJson(LeafListData<?> yangData) throws YangDataCborCodecException {
        ArrayNode arrayNode = JSON_MAPPER.createArrayNode();
        YangDataValue<?,?> value = yangData.getValue();
        if (value != null) {
            arrayNode.add(CborCodecUtil.convertToJson(value));
        }
        return arrayNode;
    }
    
    @Override
    protected LeafListData<?> buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        try {
            // For leaf-list, we need to handle array elements
            if (jsonNode != null && jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstElement = jsonNode.get(0);
                String yangText = firstElement.isValueNode() ? firstElement.asText() : firstElement.toString();
                
                // Use YangDataBuilderFactory to create the leaf-list data
                LeafListData<?> leafListData = (LeafListData<?>) org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory
                    .getBuilder()
                    .getYangData(getSchemaNode(), yangText);
                
                return leafListData;
            }
            
            // Return empty leaf-list if no elements
            return (LeafListData<?>) org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory
                .getBuilder()
                .getYangData(getSchemaNode(), null);
                
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to deserialize leaf-list data", e);
        }
    }
}
