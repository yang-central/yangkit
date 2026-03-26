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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;

/**
 * SID-aware CBOR encoder/decoder.
 * Provides high-level API for SID-based CBOR encoding as per RFC 9254 Section 5.
 * 
 * <p>RFC 9254 Section 5 defines that SID-based encoded data SHOULD be wrapped
 * in a CBOR tag (60000-60999 range) to indicate the encoding method.</p>
 * 
 * @author Yangkit Team
 */
public class SidCborEncoder {
    
    /**
     * Encodes YANG container data to CBOR bytes using SID-based encoding.
     * 
     * @param container the YANG container data
     * @param sidManager the SID manager
     * @return CBOR byte array with SID tag
     * @throws YangDataCborCodecException if encoding fails
     */
    public static byte[] encodeToCbor(org.yangcentral.yangkit.data.api.model.ContainerData container,
                                     SidManager sidManager) 
            throws YangDataCborCodecException {
        
        try {
            // Encode container children using SIDs
            ObjectNode jsonNode = SidEncoder.encodeWithSid(container, sidManager);
            
            // Wrap in CBOR tag (60000-60999 range)
            byte[] cborBytes = encodeWithTag(jsonNode, SidManager.CBOR_TAG_SID);
            
            return cborBytes;
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to encode container to CBOR with SID", e);
        }
    }
    
    /**
     * Decodes CBOR bytes to YANG container data using SID-based decoding.
     * 
     * @param cborBytes the CBOR byte array
     * @param sidManager the SID manager
     * @return decoded JSON node (ready for further processing)
     * @throws YangDataCborCodecException if decoding fails
     */
    public static JsonNode decodeFromCbor(byte[] cborBytes, SidManager sidManager) 
            throws YangDataCborCodecException {
        
        try {
            // Parse CBOR bytes
            JsonNode jsonNode = CborCodecUtil.CBOR_MAPPER.readTree(cborBytes);
            
            // Check if it's tagged with SID tag
            if (jsonNode != null && hasSidTag(jsonNode)) {
                // Remove tag and decode with SID mapping
                JsonNode untaggedNode = removeTag(jsonNode);
                return SidEncoder.decodeWithSid(untaggedNode, sidManager);
            }
            
            // No tag, decode normally
            return SidEncoder.decodeWithSid(jsonNode, sidManager);
        } catch (Exception e) {
            throw new YangDataCborCodecException("Failed to decode CBOR with SID", e);
        }
    }
    
    /**
     * Encodes a JSON node to CBOR bytes with a specific tag.
     * 
     * @param jsonNode the JSON node to encode
     * @param tag the CBOR tag to apply
     * @return CBOR byte array
     * @throws IOException if encoding fails
     */
    private static byte[] encodeWithTag(JsonNode jsonNode, int tag) throws IOException {
        byte[] cborBytes = CborCodecUtil.CBOR_MAPPER.writeValueAsBytes(jsonNode);
        
        // Prepend CBOR tag
        // CBOR tag format: major type 6 (tag), additional information based on tag value
        byte[] taggedBytes;
        if (tag < 24) {
            // Small tag value
            taggedBytes = new byte[cborBytes.length + 1];
            taggedBytes[0] = (byte) (0xC0 | tag);
            System.arraycopy(cborBytes, 0, taggedBytes, 1, cborBytes.length);
        } else if (tag < 256) {
            // Tag fits in one byte
            taggedBytes = new byte[cborBytes.length + 2];
            taggedBytes[0] = (byte) 0xD8; // tag 1-byte follow
            taggedBytes[1] = (byte) tag;
            System.arraycopy(cborBytes, 0, taggedBytes, 2, cborBytes.length);
        } else if (tag < 65536) {
            // Tag fits in two bytes
            taggedBytes = new byte[cborBytes.length + 3];
            taggedBytes[0] = (byte) 0xD9; // tag 2-bytes follow
            taggedBytes[1] = (byte) ((tag >> 8) & 0xFF);
            taggedBytes[2] = (byte) (tag & 0xFF);
            System.arraycopy(cborBytes, 0, taggedBytes, 3, cborBytes.length);
        } else {
            // Large tag value (4 bytes)
            taggedBytes = new byte[cborBytes.length + 5];
            taggedBytes[0] = (byte) 0xDA; // tag 4-bytes follow
            taggedBytes[1] = (byte) ((tag >> 24) & 0xFF);
            taggedBytes[2] = (byte) ((tag >> 16) & 0xFF);
            taggedBytes[3] = (byte) ((tag >> 8) & 0xFF);
            taggedBytes[4] = (byte) (tag & 0xFF);
            System.arraycopy(cborBytes, 0, taggedBytes, 5, cborBytes.length);
        }
        
        return taggedBytes;
    }
    
    /**
     * Checks if a JSON node has a SID tag.
     * Note: This is a simplified check. Full implementation would parse CBOR structure.
     * 
     * @param jsonNode the JSON node
     * @return true if the node appears to have a SID tag
     */
    private static boolean hasSidTag(JsonNode jsonNode) {
        // Simplified implementation
        // In practice, tags are part of CBOR encoding and may not be visible in JSON representation
        // Full implementation would need to work with raw CBOR bytes
        return false;
    }
    
    /**
     * Removes CBOR tag from a parsed node.
     * 
     * @param jsonNode the tagged JSON node
     * @return the untagged JSON node
     */
    private static JsonNode removeTag(JsonNode jsonNode) {
        // Tags are handled during CBOR parsing
        // Return the node as-is
        return jsonNode;
    }
}
