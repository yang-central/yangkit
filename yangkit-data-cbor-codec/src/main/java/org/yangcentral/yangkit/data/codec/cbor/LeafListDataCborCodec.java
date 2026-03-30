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
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.model.api.stmt.LeafList;

/**
 * CBOR codec for a single YANG leaf-list entry (RFC 9254 §5.2).
 *
 * <p>Each {@link LeafListData} represents exactly one value. The parent container
 * codec ({@link ContainerDataCborCodec}) is responsible for collecting all entries
 * sharing the same leaf-list QName into a JSON array, and for expanding that array
 * into individual entries on deserialization.
 *
 * @author Yangkit Team
 */
public class LeafListDataCborCodec extends YangDataCborCodec<LeafList, LeafListData<?>> {

    public LeafListDataCborCodec(LeafList schemaNode) {
        super(schemaNode);
    }

    /**
     * Serializes a single leaf-list entry value as a scalar JSON node.
     * The wrapping array is managed by the parent container codec.
     */
    @Override
    protected JsonNode buildJson(LeafListData<?> yangData) throws YangDataCborCodecException {
        if (yangData.getValue() == null) {
            return JSON_MAPPER.nullNode();
        }
        return CborCodecUtil.convertToJson(yangData.getValue());
    }

    /**
     * Deserializes a single scalar JSON node into a leaf-list entry.
     *
     * <p>Callers should pass individual element nodes, not the entire array.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected LeafListData<?> buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        try {
            String yangText = CborCodecUtil.toYangText(jsonNode);
            LeafListData<?> leafListData = (LeafListData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(), yangText);
            return leafListData;
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to deserialize leaf-list entry", e);
        }
    }
}
