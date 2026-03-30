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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * SID-aware CBOR encoder/decoder using direct {@link CBORGenerator} output.
 *
 * <p>This implementation produces RFC 9254 §3.3-compliant SID-based CBOR:
 * <ul>
 *   <li>Map keys are CBOR <em>integers</em> (not text strings).</li>
 *   <li>Keys are <em>delta-encoded</em>: each key is the difference between the
 *       node's SID and its parent node's SID (root parent SID = 0). Within a
 *       single map siblings are ordered by ascending SID and deltas are computed
 *       cumulatively from the previous sibling.</li>
 *   <li>The outermost payload is tagged with CBOR tag 272 (the IANA-registered
 *       tag for YANG-CBOR SID-encoded content) via raw byte prepending.</li>
 * </ul>
 *
 * <p><b>Known limitations</b> (out of scope for this fix):
 * <ul>
 *   <li>decimal64 is encoded as text string instead of CBOR tag 4.</li>
 *   <li>bits is encoded as space-separated text instead of a byte string.</li>
 * </ul>
 *
 * @author Yangkit Team
 */
public class SidCborEncoder {

    /**
     * IANA-registered CBOR tag for YANG SID-encoded data (RFC 9254 §9.2).
     */
    public static final int YANG_CBOR_SID_TAG = 272;

    private static final CBORFactory CBOR_FACTORY = new CBORFactory();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Encodes a container to CBOR bytes using SID-based integer map keys with
     * delta encoding (RFC 9254 §3.3). The payload is wrapped in CBOR tag 272.
     *
     * @param container  the YANG container to encode
     * @param sidManager SID manager for QName→SID lookup
     * @return CBOR bytes with tag 272 prefix
     * @throws YangDataCborCodecException on encoding error
     */
    public static byte[] encodeToCbor(ContainerData container, SidManager sidManager)
            throws YangDataCborCodecException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (CBORGenerator gen = CBOR_FACTORY.createGenerator(baos)) {
                writeContainer(gen, container, sidManager, 0L);
            }
            byte[] payload = baos.toByteArray();
            return prependTag(payload, YANG_CBOR_SID_TAG);
        } catch (IOException e) {
            throw new YangDataCborCodecException("Failed to SID-encode container", e);
        }
    }

    /**
     * Decodes CBOR bytes (possibly tagged with tag 272) that were encoded with
     * SID-based integer keys into a resolved {@link ObjectNode} with original
     * local names as string keys. The result can then be fed into
     * {@link ContainerDataCborCodec#deserialize}.
     *
     * @param cborBytes  CBOR bytes (with or without tag 272 prefix)
     * @param sidManager SID manager for SID→QName lookup
     * @return ObjectNode with resolved string keys
     * @throws YangDataCborCodecException on decoding error
     */
    public static JsonNode decodeFromCbor(byte[] cborBytes, SidManager sidManager)
            throws YangDataCborCodecException {
        try {
            byte[] payload = stripTag(cborBytes, YANG_CBOR_SID_TAG);
            JsonNode raw = CborCodecUtil.CBOR_MAPPER.readTree(payload);
            return resolveIntegerKeys(raw, sidManager);
        } catch (IOException e) {
            throw new YangDataCborCodecException("Failed to decode SID CBOR", e);
        }
    }

    // -----------------------------------------------------------------------
    // Encoding helpers
    // -----------------------------------------------------------------------

    /**
     * Writes a {@link YangDataContainer} as a CBOR map with SID delta-encoded
     * integer keys.
     *
     * @param gen         CBOR generator
     * @param container   container whose children to write
     * @param sidManager  SID lookup
     * @param parentSid   SID of the parent node (0 for the root)
     */
    static void writeContainer(CBORGenerator gen, YangDataContainer container,
                               SidManager sidManager, long parentSid) throws IOException {

        List<YangData<?>> children = container.getDataChildren();

        // Group by QName to collect list/leaf-list siblings
        Map<String, List<YangData<?>>> groups = new LinkedHashMap<>();
        for (YangData<?> child : children) {
            if (child == null) continue;
            String key = child.getQName().getLocalName();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(child);
        }

        // Sort groups by SID for delta encoding
        List<Map.Entry<String, List<YangData<?>>>> sorted = new ArrayList<>(groups.entrySet());
        sorted.sort((a, b) -> {
            Long sidA = sidManager.getSid(a.getValue().get(0).getQName());
            Long sidB = sidManager.getSid(b.getValue().get(0).getQName());
            if (sidA == null) sidA = Long.MAX_VALUE;
            if (sidB == null) sidB = Long.MAX_VALUE;
            return Long.compare(sidA, sidB);
        });

        gen.writeStartObject(sorted.size());
        long prevSid = parentSid;

        for (Map.Entry<String, List<YangData<?>>> entry : sorted) {
            List<YangData<?>> group = entry.getValue();
            YangData<?> first = group.get(0);
            QName qName = first.getQName();
            Long sid = sidManager.getSid(qName);

            long delta = (sid != null ? sid : 0L) - prevSid;
            gen.writeFieldId(delta);
            if (sid != null) prevSid = sid;

            if (first instanceof LeafListData) {
                gen.writeStartArray();
                for (YangData<?> item : group) {
                    writeScalarValue(gen, ((LeafListData<?>) item).getValue());
                }
                gen.writeEndArray();

            } else if (first instanceof ListData) {
                gen.writeStartArray();
                for (YangData<?> item : group) {
                    writeContainer(gen, (ListData) item, sidManager,
                            sid != null ? sid : 0L);
                }
                gen.writeEndArray();

            } else if (first instanceof LeafData) {
                writeScalarValue(gen, ((LeafData<?>) first).getValue());

            } else if (first instanceof YangDataContainer) {
                writeContainer(gen, (YangDataContainer) first, sidManager,
                        sid != null ? sid : 0L);
            }
        }

        gen.writeEndObject();
    }

    /** Writes a single typed scalar value to the generator. */
    private static void writeScalarValue(CBORGenerator gen, YangDataValue<?, ?> value)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        Object val;
        try {
            val = value.getValue();
        } catch (Exception e) {
            try {
                gen.writeString(value.getStringValue());
            } catch (Exception ex) {
                gen.writeNull();
            }
            return;
        }
        if (val == null) {
            gen.writeNull();
        } else if (val instanceof Boolean) {
            gen.writeBoolean((Boolean) val);
        } else if (val instanceof byte[]) {
            gen.writeBinary((byte[]) val);
        } else if (val instanceof Integer) {
            gen.writeNumber((Integer) val);
        } else if (val instanceof Long) {
            gen.writeNumber((Long) val);
        } else if (val instanceof java.math.BigDecimal) {
            // text string to preserve precision (CBOR tag 4 deferred)
            gen.writeString(val.toString());
        } else if (val instanceof List) {
            // bits
            StringBuilder sb = new StringBuilder();
            for (Object bit : (List<?>) val) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(bit.toString());
            }
            gen.writeString(sb.toString());
        } else {
            gen.writeString(val.toString());
        }
    }

    // -----------------------------------------------------------------------
    // Decoding helpers
    // -----------------------------------------------------------------------

    /**
     * Recursively resolves integer keys in a decoded CBOR JsonNode back to
     * string local-names using the SID manager.
     */
    private static JsonNode resolveIntegerKeys(JsonNode node, SidManager sidManager) {
        if (node == null) return CborCodecUtil.JSON_MAPPER.nullNode();
        if (node.isObject()) {
            ObjectNode result = CborCodecUtil.JSON_MAPPER.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                String resolved = resolveKey(e.getKey(), sidManager);
                result.set(resolved, resolveIntegerKeys(e.getValue(), sidManager));
            }
            return result;
        }
        if (node.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode arr =
                    CborCodecUtil.JSON_MAPPER.createArrayNode();
            for (JsonNode element : node) {
                arr.add(resolveIntegerKeys(element, sidManager));
            }
            return arr;
        }
        return node;
    }

    private static String resolveKey(String key, SidManager sidManager) {
        try {
            Long sid = Long.parseLong(key);
            QName qName = sidManager.getQName(sid);
            if (qName != null) return qName.getLocalName();
        } catch (NumberFormatException ignored) {
        }
        return key;
    }

    // -----------------------------------------------------------------------
    // CBOR tag helpers
    // -----------------------------------------------------------------------

    /**
     * Prepends a CBOR tag to a payload byte array following RFC 7049 §2.4.
     *
     * @param payload the payload bytes
     * @param tag     the tag value
     * @return new byte array with the tag header prepended
     */
    public static byte[] prependTag(byte[] payload, int tag) {
        byte[] header;
        if (tag < 24) {
            header = new byte[]{(byte) (0xC0 | tag)};
        } else if (tag < 256) {
            header = new byte[]{(byte) 0xD8, (byte) tag};
        } else if (tag < 65536) {
            header = new byte[]{(byte) 0xD9, (byte) ((tag >> 8) & 0xFF), (byte) (tag & 0xFF)};
        } else {
            header = new byte[]{(byte) 0xDA,
                    (byte) ((tag >> 24) & 0xFF), (byte) ((tag >> 16) & 0xFF),
                    (byte) ((tag >> 8) & 0xFF), (byte) (tag & 0xFF)};
        }
        byte[] result = new byte[header.length + payload.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(payload, 0, result, header.length, payload.length);
        return result;
    }

    /**
     * Strips the CBOR tag prefix from the given bytes if present; otherwise
     * returns the bytes unchanged.
     *
     * @param bytes   CBOR bytes possibly tagged
     * @param tag     expected tag value
     * @return payload bytes without the tag header
     */
    public static byte[] stripTag(byte[] bytes, int tag) {
        if (bytes == null || bytes.length == 0) return bytes;
        int first = bytes[0] & 0xFF;
        // major type 6 (tag) = 0xC0..0xDB range
        if ((first & 0xE0) != 0xC0) return bytes; // not a tag
        int headerLen;
        int decodedTag;
        int ai = first & 0x1F;
        if (ai < 24) {
            decodedTag = ai;
            headerLen = 1;
        } else if (ai == 24 && bytes.length >= 2) {
            decodedTag = bytes[1] & 0xFF;
            headerLen = 2;
        } else if (ai == 25 && bytes.length >= 3) {
            decodedTag = ((bytes[1] & 0xFF) << 8) | (bytes[2] & 0xFF);
            headerLen = 3;
        } else if (ai == 26 && bytes.length >= 5) {
            decodedTag = ((bytes[1] & 0xFF) << 24) | ((bytes[2] & 0xFF) << 16)
                    | ((bytes[3] & 0xFF) << 8) | (bytes[4] & 0xFF);
            headerLen = 5;
        } else {
            return bytes;
        }
        if (decodedTag != tag) return bytes;
        return Arrays.copyOfRange(bytes, headerLen, bytes.length);
    }
}
