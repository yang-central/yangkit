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
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

/**
 * CBOR codec for YANG leaf data.
 *
 * @author Yangkit Team
 */
public class LeafDataCborCodec extends YangDataCborCodec<Leaf, LeafData<?>> {
    
    public LeafDataCborCodec(Leaf schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected JsonNode buildJson(LeafData<?> yangData) throws YangDataCborCodecException {
        YangDataValue<?,?> value = yangData.getValue();
        if (value != null) {
            return CborCodecUtil.convertToJson(value);
        }
        throw new YangDataCborCodecException("Invalid leaf value: null");
    }
    
    @Override
    protected LeafData<?> buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        try {
            // Use CborCodecUtil to convert JSON node to proper Java type
            Object value = CborCodecUtil.convertFromJson(jsonNode, null);
            
            // Convert value to string representation for YangDataBuilderFactory
            String yangText = value != null ? value.toString() : null;
            
            // Use YangDataBuilderFactory to create the leaf data with proper type
            LeafData leafData = (LeafData) org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory
                .getBuilder()
                .getYangData(getSchemaNode(), yangText);
            
            return leafData;
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to deserialize leaf data", e);
        }
    }
}
