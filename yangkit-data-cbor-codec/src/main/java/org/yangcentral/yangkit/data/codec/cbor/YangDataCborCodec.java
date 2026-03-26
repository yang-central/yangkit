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
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

/**
 * Abstract base class for YANG CBOR codec implementations.
 * Provides common functionality for encoding/decoding YANG data to/from CBOR format.
 * 
 * <p>This class follows RFC 9254 - YANG Data Model in CBOR.</p>
 *
 * @param <N> Schema node type
 * @param <D> YangData type
 * @author Yangkit Team
 */
public abstract class YangDataCborCodec<N extends SchemaNode, D extends YangData<?>> {
    
    protected static final ObjectMapper CBOR_MAPPER = new ObjectMapper(new CBORFactory());
    protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private final N schemaNode;
    
    /**
     * Constructs a new CBOR codec for the given schema node.
     *
     * @param schemaNode the schema node
     */
    protected YangDataCborCodec(N schemaNode) {
        this.schemaNode = schemaNode;
    }
    
    /**
     * Gets the schema node associated with this codec.
     *
     * @return the schema node
     */
    protected N getSchemaNode() {
        return schemaNode;
    }
    
    /**
     * Serializes YANG data to CBOR bytes.
     *
     * @param yangData the YANG data to serialize
     * @return CBOR byte array
     * @throws YangDataCborCodecException if serialization fails
     */
    public byte[] serialize(D yangData) throws YangDataCborCodecException {
        try {
            JsonNode jsonNode = buildJson(yangData);
            if (jsonNode == null) {
                throw new YangDataCborCodecException("Failed to build JSON node for yangData: " + yangData);
            }
            return CBOR_MAPPER.writeValueAsBytes(jsonNode);
        } catch (JsonProcessingException e) {
            throw new YangDataCborCodecException("Failed to serialize yangData to CBOR", e);
        }
    }
    
    /**
     * Deserializes CBOR bytes to YANG data.
     *
     * @param cborData the CBOR byte array
     * @param validatorResultBuilder the validator result builder
     * @return deserialized YANG data
     * @throws YangDataCborCodecException if deserialization fails
     */
    public D deserialize(byte[] cborData, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        try {
            JsonNode jsonNode = CBOR_MAPPER.readTree(cborData);
            return buildData(jsonNode, validatorResultBuilder);
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to deserialize CBOR data", e);
        }
    }
    
    /**
     * Builds JSON structure from YANG data.
     *
     * @param yangData the YANG data
     * @return JSON node representing the data
     * @throws YangDataCborCodecException if building fails
     */
    protected abstract JsonNode buildJson(D yangData) throws YangDataCborCodecException;
    
    /**
     * Builds YANG data from JSON structure.
     *
     * @param jsonNode the JSON node
     * @param validatorResultBuilder the validator result builder
     * @return deserialized YANG data
     * @throws YangDataCborCodecException if building fails
     */
    protected abstract D buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException;
}
