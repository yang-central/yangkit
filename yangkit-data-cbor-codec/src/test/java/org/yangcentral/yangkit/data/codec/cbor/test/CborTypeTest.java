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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.codec.cbor.LeafDataCborCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests type-accurate CBOR encoding/decoding for each YANG data type
 * as required by RFC 9254 §4.
 *
 * @author Yangkit Team
 */
public class CborTypeTest {

    private static Module testModule;

    @BeforeAll
    static void setup() throws Exception {
        URL yangUrl = CborTypeTest.class.getClassLoader().getResource("test-module.yang");
        assertNotNull(yangUrl, "test-module.yang must be on the classpath");
        YangSchemaContext ctx = YangYinParser.parse(yangUrl.getPath());
        assertTrue(ctx.validate().isOk(), "Schema validation failed");
        testModule = ctx.getLatestModule("test-module")
                .orElseThrow(() -> new AssertionError("test-module not found"));
    }

    private Leaf getLeaf(String name) {
        return (Leaf) testModule.getDataDefChild(name);
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.10 – string → CBOR text string
    // ------------------------------------------------------------------

    @Test
    void testStringRoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-string");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "hello CBOR");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertTrue(encoded.length > 0);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("hello CBOR", decoded.getStringValue());
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.8 – int32 → CBOR signed integer
    // ------------------------------------------------------------------

    @Test
    void testInt32RoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-integer");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "-12345");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);

        // CBOR encoding of a small negative integer should fit in a few bytes
        assertTrue(encoded.length <= 5, "int32 CBOR encoding should be compact");

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("-12345", decoded.getStringValue());
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.2 – boolean → CBOR true/false
    // ------------------------------------------------------------------

    @Test
    void testBooleanTrue() throws Exception {
        Leaf leaf = getLeaf("test-boolean");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "true");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        // CBOR true = 0xF5 (1 byte)
        assertEquals(1, encoded.length);
        assertEquals((byte) 0xF5, encoded[0]);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("true", decoded.getStringValue());
    }

    @Test
    void testBooleanFalse() throws Exception {
        Leaf leaf = getLeaf("test-boolean");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "false");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        // CBOR false = 0xF4 (1 byte)
        assertEquals(1, encoded.length);
        assertEquals((byte) 0xF4, encoded[0]);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertEquals("false", decoded.getStringValue());
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.3 – decimal64 → text string (precision-safe)
    // Note: RFC 9254 prefers CBOR tag 4; text string is the current
    // implementation choice. Tag 4 support is a known limitation.
    // ------------------------------------------------------------------

    @Test
    void testDecimal64RoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-decimal");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "123.456789");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertTrue(encoded.length > 0);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals(new BigDecimal("123.456789"),
                new BigDecimal(decoded.getStringValue()));
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.12 – binary → CBOR byte string (major type 2)
    // ------------------------------------------------------------------

    @Test
    void testBinaryRoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-binary");
        assertNotNull(leaf);

        byte[] rawBytes = {0x01, 0x02, 0x03, (byte) 0xFF};
        String base64 = Base64.getEncoder().encodeToString(rawBytes);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, base64);
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertTrue(encoded.length > 0);

        // CBOR byte string major type = 0x40 | length
        // For 4 bytes: first byte should be 0x44
        assertEquals((byte) 0x44, encoded[0],
                "CBOR byte string of 4 bytes should start with 0x44");

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);

        // Re-decode: value should round-trip to the same base64
        byte[] decodedBytes = Base64.getDecoder().decode(decoded.getStringValue());
        assertArrayEquals(rawBytes, decodedBytes);
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.1 – bits → CBOR text string (space-separated names)
    // Note: RFC 9254 prefers CBOR byte string; text is the current choice.
    // ------------------------------------------------------------------

    @Test
    void testBitsRoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-bits");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "bit0 bit2");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertTrue(encoded.length > 0);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("bit0 bit2", decoded.getStringValue());
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.6 – empty → CBOR null (0xF6)
    // ------------------------------------------------------------------

    @Test
    void testEmptyType() throws Exception {
        Leaf leaf = getLeaf("test-empty");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertEquals(1, encoded.length, "CBOR null is 1 byte");
        assertEquals((byte) 0xF6, encoded[0], "CBOR null = 0xF6");
    }

    // ------------------------------------------------------------------
    // RFC 9254 §4.5 – enumeration → CBOR text string
    // ------------------------------------------------------------------

    @Test
    void testEnumerationRoundTrip() throws Exception {
        Leaf leaf = getLeaf("test-enum");
        assertNotNull(leaf);

        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(leaf, "green");
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);

        byte[] encoded = codec.serialize(data);
        assertTrue(encoded.length > 0);

        LeafData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("green", decoded.getStringValue());
    }
}
