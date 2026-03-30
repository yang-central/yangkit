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
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.restriction.Binary;
import org.yangcentral.yangkit.model.api.restriction.Empty;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

import java.io.IOException;
import java.util.Base64;

/**
 * CBOR codec for a YANG leaf node (RFC 9254 §4).
 *
 * <p>Type mapping:
 * <ul>
 *   <li>empty  → CBOR null (0xF6), serialized as JSON NullNode</li>
 *   <li>binary → CBOR byte string (major type 2), via Jackson BinaryNode</li>
 *   <li>decimal64 → CBOR text string (precision-safe; tag 4 deferred)</li>
 *   <li>bits   → CBOR text string (space-separated active bit names)</li>
 *   <li>boolean → CBOR true/false</li>
 *   <li>integer types → CBOR signed/unsigned integer</li>
 *   <li>string, enumeration, identityref → CBOR text string</li>
 * </ul>
 *
 * @author Yangkit Team
 */
public class LeafDataCborCodec extends YangDataCborCodec<Leaf, LeafData<?>> {

    public LeafDataCborCodec(Leaf schemaNode) {
        super(schemaNode);
    }

    @Override
    protected JsonNode buildJson(LeafData<?> yangData) throws YangDataCborCodecException {
        Restriction<?> restriction = getSchemaNode().getType().getRestriction();

        // empty type → CBOR null
        if (restriction instanceof Empty) {
            return JSON_MAPPER.nullNode();
        }

        if (yangData.getValue() == null) {
            throw new YangDataCborCodecException("Null value in leaf: " + getSchemaNode().getArgStr());
        }

        return CborCodecUtil.convertToJson(yangData.getValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LeafData<?> buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        try {
            Restriction<?> restriction = getSchemaNode().getType().getRestriction();

            // empty type: any null node means empty
            if (restriction instanceof Empty) {
                return (LeafData<?>) YangDataBuilderFactory.getBuilder()
                        .getYangData(getSchemaNode(), "");
            }

            String yangText = toYangText(jsonNode, restriction);
            return (LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(), yangText);
        } catch (YangDataCborCodecException e) {
            throw e;
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to deserialize leaf data", e);
        }
    }

    /**
     * Converts a decoded CBOR JsonNode to the YANG text representation expected
     * by {@link YangDataBuilderFactory}.
     */
    private String toYangText(JsonNode jsonNode, Restriction<?> restriction)
            throws YangDataCborCodecException {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        // binary → CBOR byte string was decoded as BinaryNode by Jackson CBOR;
        // the builder expects base64-encoded text
        if (restriction instanceof Binary) {
            if (jsonNode.isBinary()) {
                try {
                    return Base64.getEncoder().encodeToString(jsonNode.binaryValue());
                } catch (IOException e) {
                    throw new YangDataCborCodecException("Failed to read binary value", e);
                }
            }
            // Fallback: if somehow received as text (e.g. old encoding)
            return jsonNode.asText();
        }

        // All other types: use the text representation
        return jsonNode.asText();
    }
}
