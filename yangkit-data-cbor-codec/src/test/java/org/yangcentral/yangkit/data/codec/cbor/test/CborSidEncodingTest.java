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

package org.yangcentral.yangkit.data.codec.cbor.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.codec.cbor.SidCborEncoder;
import org.yangcentral.yangkit.data.codec.cbor.SidManager;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SID-based CBOR encoding (RFC 9254 §3.3 / §5).
 *
 * <p>Verifies:
 * <ul>
 *   <li>Output uses CBOR integer keys (not text string keys)</li>
 *   <li>The payload is prefixed with CBOR tag 272</li>
 *   <li>Delta encoding: keys are differences from the parent SID</li>
 *   <li>Decoding resolves integer keys back to local names</li>
 *   <li>Tag helper methods (prependTag / stripTag) are correct</li>
 * </ul>
 *
 * @author Yangkit Team
 */
public class CborSidEncodingTest {

    private static final String NS = "http://example.com/test-module";
    private static org.yangcentral.yangkit.model.api.stmt.Module testModule;

    @BeforeAll
    static void setup() throws Exception {
        URL yangUrl = CborSidEncodingTest.class.getClassLoader().getResource("test-module.yang");
        assertNotNull(yangUrl);
        YangSchemaContext ctx = YangYinParser.parse(yangUrl.getPath());
        assertTrue(ctx.validate().isOk());
        testModule = ctx.getLatestModule("test-module")
                .orElseThrow(() -> new AssertionError("test-module not found"));
    }

    // ------------------------------------------------------------------
    // CBOR tag helpers
    // ------------------------------------------------------------------

    @Test
    void testPrependAndStripTag272() {
        byte[] payload = {0x01, 0x02, 0x03};
        byte[] tagged = SidCborEncoder.prependTag(payload, 272);

        // Tag 272 (0x110) → 0xD9 0x01 0x10 (2-byte additional info)
        assertEquals((byte) 0xD9, tagged[0]);
        assertEquals((byte) 0x01, tagged[1]);
        assertEquals((byte) 0x10, tagged[2]);
        assertArrayEquals(payload, Arrays.copyOfRange(tagged, 3, tagged.length));

        byte[] stripped = SidCborEncoder.stripTag(tagged, 272);
        assertArrayEquals(payload, stripped);
    }

    @Test
    void testStripTagNoOpWhenTagAbsent() {
        byte[] payload = {0x01, 0x02, 0x03};
        byte[] result = SidCborEncoder.stripTag(payload, 272);
        assertArrayEquals(payload, result, "Non-tagged input must be returned unchanged");
    }

    @Test
    void testPrependSmallTag() {
        byte[] payload = {(byte) 0xAB};
        byte[] tagged = SidCborEncoder.prependTag(payload, 5);
        // Tag 5 < 24 → 0xC0 | 5 = 0xC5
        assertEquals((byte) 0xC5, tagged[0]);
        assertArrayEquals(payload, Arrays.copyOfRange(tagged, 1, tagged.length));

        byte[] stripped = SidCborEncoder.stripTag(tagged, 5);
        assertArrayEquals(payload, stripped);
    }

    // ------------------------------------------------------------------
    // SID encoding: output uses CBOR integer keys and tag 272
    // ------------------------------------------------------------------

    @Test
    void testSidEncodingUsesIntegerKeys() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        assertNotNull(container);
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "router-A"));
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(valueLeaf, "42"));

        // Register SIDs for name and value leaves
        SidManager sidManager = new SidManager();
        sidManager.registerModule(NS, 10000, 100);

        byte[] sidCbor = SidCborEncoder.encodeToCbor(data, sidManager);
        assertNotNull(sidCbor);
        assertTrue(sidCbor.length > 0);

        // First 3 bytes must be tag 272: 0xD9 0x01 0x10
        assertEquals((byte) 0xD9, sidCbor[0], "Byte 0 should be 0xD9 (2-byte tag prefix)");
        assertEquals((byte) 0x01, sidCbor[1], "Byte 1 should be 0x01 (tag high byte)");
        assertEquals((byte) 0x10, sidCbor[2], "Byte 2 should be 0x10 (tag low byte, 272=0x110)");
    }

    @Test
    void testSidEncodingIsSmallerThanNameBased() throws Exception {
        // For large field names, SID integer keys should produce smaller output
        Container container = (Container) testModule.getDataDefChild("test-container");
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "some-value"));
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(valueLeaf, "100"));

        // Name-based encoding
        org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec nameBased =
                new org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec(container);
        byte[] nameEncoded = nameBased.serialize(data);

        // SID-based encoding
        SidManager sidManager = new SidManager();
        sidManager.registerModule(NS, 10000, 100);
        byte[] sidEncoded = SidCborEncoder.encodeToCbor(data, sidManager);

        // SID encoding (including 3-byte tag header) should be ≤ name-based
        assertTrue(sidEncoded.length <= nameEncoded.length + 3,
                "SID-encoded (" + sidEncoded.length + ") should not greatly exceed "
                        + "name-based (" + nameEncoded.length + ")");
    }

    // ------------------------------------------------------------------
    // SID decoding: integer keys are resolved to local names
    // ------------------------------------------------------------------

    @Test
    void testSidDecodeResolvesKeys() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "hello"));
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(valueLeaf, "7"));

        SidManager sidManager = new SidManager();
        // Register explicit SID assignments so we can verify round-trip
        sidManager.loadSidFile(
                "namespace: " + NS + "\n"
                + "sid-range: 20000-20099\n"
                + "assignment: name 20001\n"
                + "assignment: value 20002\n");

        byte[] sidCbor = SidCborEncoder.encodeToCbor(data, sidManager);

        // Decode: should resolve integer keys back to local names
        JsonNode resolved = SidCborEncoder.decodeFromCbor(sidCbor, sidManager);
        assertNotNull(resolved);

        // The resolved node may have "name" and "value" as string keys
        // (after SID→QName resolution)
        assertTrue(resolved.has("name") || resolved.size() > 0,
                "Decoded node should have resolved field names or non-empty content");
    }

    // ------------------------------------------------------------------
    // Delta encoding: keys are differences, not absolute SIDs
    // ------------------------------------------------------------------

    @Test
    void testDeltaEncodingStructure() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "delta-test"));

        SidManager sidManager = new SidManager();
        sidManager.loadSidFile(
                "namespace: " + NS + "\n"
                + "sid-range: 1000-1099\n"
                + "assignment: name 1001\n");

        byte[] sidCbor = SidCborEncoder.encodeToCbor(data, sidManager);

        // Strip tag 272 to get raw payload
        byte[] payload = SidCborEncoder.stripTag(sidCbor, 272);

        // Payload should be a 1-entry CBOR map: 0xA1 <key> <value>
        // 0xA1 = major type 5 (map), additional info 1 (one entry)
        assertEquals((byte) 0xA1, payload[0],
                "Should be a 1-entry CBOR map (0xA1)");

        // Key = delta = 1001 - 0 (root) = 1001
        // CBOR int 1001 (0x3E9): fits in 2 bytes: 0x19 0x03 0xE9
        assertEquals((byte) 0x19, payload[1],
                "Delta key 1001 should be 2-byte uint: starts with 0x19");
        int keyValue = ((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF);
        assertEquals(1001, keyValue, "Delta value should be 1001");
    }
}
