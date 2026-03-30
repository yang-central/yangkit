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
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec;
import org.yangcentral.yangkit.data.codec.cbor.LeafListDataCborCodec;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for list and leaf-list CBOR encoding/decoding (RFC 9254 §5.2–5.3).
 *
 * @author Yangkit Team
 */
public class CborListTest {

    private static org.yangcentral.yangkit.model.api.stmt.Module testModule;

    @BeforeAll
    static void setup() throws Exception {
        URL yangUrl = CborListTest.class.getClassLoader().getResource("test-module.yang");
        assertNotNull(yangUrl);
        YangSchemaContext ctx = YangYinParser.parse(yangUrl.getPath());
        assertTrue(ctx.validate().isOk());
        testModule = ctx.getLatestModule("test-module")
                .orElseThrow(() -> new AssertionError("test-module not found"));
    }

    // ------------------------------------------------------------------
    // Leaf-list: multiple entries are grouped into a CBOR array
    // ------------------------------------------------------------------

    @Test
    void testLeafListSingleEntry() throws Exception {
        LeafList ll = (LeafList) testModule.getDataDefChild("test-leaf-list");
        assertNotNull(ll);

        LeafListData<?> entry = (LeafListData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(ll, "alpha");
        LeafListDataCborCodec codec = new LeafListDataCborCodec(ll);

        byte[] encoded = codec.serialize(entry);
        assertTrue(encoded.length > 0);

        LeafListData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);
        assertEquals("alpha", decoded.getStringValue());
    }

    @Test
    void testLeafListMultipleEntriesViaContainer() throws Exception {
        // RFC 9254 §5.2: multiple leaf-list entries share the same field name
        // and are encoded as a CBOR array by the container codec.
        // Verify the leaf-list codec round-trips each entry correctly.
        LeafList ll = (LeafList) testModule.getDataDefChild("test-leaf-list");
        assertNotNull(ll);

        String[] values = {"alpha", "beta", "gamma"};
        LeafListDataCborCodec codec = new LeafListDataCborCodec(ll);
        for (String v : values) {
            LeafListData<?> entry = (LeafListData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(ll, v);
            byte[] encoded = codec.serialize(entry);
            LeafListData<?> decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
            assertNotNull(decoded);
            assertEquals(v, decoded.getStringValue());
        }
    }

    // ------------------------------------------------------------------
    // List: single entry round-trip
    // ------------------------------------------------------------------

    @Test
    void testListSingleEntryRoundTrip() throws Exception {
        YangList listSchema = (YangList) testModule.getDataDefChild("test-list");
        assertNotNull(listSchema);
        Leaf idLeaf = (Leaf) listSchema.getDataDefChild("id");
        Leaf nameLeaf = (Leaf) listSchema.getDataDefChild("name");

        // Build key list
        LeafData<?> idData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(idLeaf, "1");
        List<LeafData> keys = new ArrayList<>();
        keys.add(idData);

        @SuppressWarnings("unchecked")
        ListData entry = (ListData) YangDataBuilderFactory.getBuilder()
                .getYangData(listSchema, keys);
        assertNotNull(entry, "Builder should return a ListData");

        // Add non-key child
        LeafData<?> nameData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "router-01");
        entry.addChild(nameData);

        // The ListDataCborCodec serializes ONE entry as an object
        org.yangcentral.yangkit.data.codec.cbor.ListDataCborCodec codec =
                new org.yangcentral.yangkit.data.codec.cbor.ListDataCborCodec(listSchema);
        byte[] encoded = codec.serialize(entry);
        assertTrue(encoded.length > 0, "Serialized list entry should not be empty");

        ListData decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);

        // Key should be present
        List<LeafData> decodedKeys = decoded.getKeys();
        assertFalse(decodedKeys.isEmpty(), "Decoded list entry must have keys");
        assertEquals("1", decodedKeys.get(0).getStringValue());

        // Non-key child should also be present
        List<YangData<?>> children = decoded.getDataChildren();
        boolean foundName = false;
        for (YangData<?> child : children) {
            if ("name".equals(child.getQName().getLocalName())) {
                assertEquals("router-01", ((LeafData<?>) child).getStringValue());
                foundName = true;
            }
        }
        assertTrue(foundName, "Non-key field 'name' should be deserialized");
    }

    // ------------------------------------------------------------------
    // List inside container: multiple entries → CBOR array
    // ------------------------------------------------------------------

    @Test
    void testContainerWithMultipleListEntries() throws Exception {
        Container networkContainer = (Container) testModule.getDataDefChild("network");
        assertNotNull(networkContainer);
        YangList ifaceList = (YangList) networkContainer.getDataDefChild("interface");
        assertNotNull(ifaceList);
        Leaf nameLeaf = (Leaf) ifaceList.getDataDefChild("name");
        Leaf enabledLeaf = (Leaf) ifaceList.getDataDefChild("enabled");
        Leaf mtuLeaf = (Leaf) ifaceList.getDataDefChild("mtu");

        ContainerDataImpl network = new ContainerDataImpl(networkContainer);
        network.setQName(networkContainer.getIdentifier());

        // Add 2 interface entries
        String[][] ifaceData = {
            {"eth0", "true",  "1500"},
            {"eth1", "false", "9000"}
        };
        for (String[] row : ifaceData) {
            LeafData<?> keyData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(nameLeaf, row[0]);
            List<LeafData> keys = new ArrayList<>();
            keys.add(keyData);
            @SuppressWarnings("unchecked")
            ListData iface = (ListData) YangDataBuilderFactory.getBuilder()
                    .getYangData(ifaceList, keys);
            iface.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(enabledLeaf, row[1]));
            iface.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(mtuLeaf, row[2]));
            network.addChild(iface);
        }

        ContainerDataCborCodec codec = new ContainerDataCborCodec(networkContainer);
        byte[] encoded = codec.serialize(network);
        assertTrue(encoded.length > 0);

        ContainerData decoded = codec.deserialize(encoded, new ValidatorResultBuilder());
        assertNotNull(decoded);

        // Should have 2 interface list entries
        List<YangData<?>> children = decoded.getDataChildren();
        long listCount = children.stream()
                .filter(c -> c instanceof ListData)
                .count();
        assertEquals(2, listCount, "Container should have 2 list entries after round-trip");

        // Verify first entry key
        ListData first = (ListData) children.stream()
                .filter(c -> c instanceof ListData)
                .findFirst().get();
        assertFalse(first.getKeys().isEmpty());
        assertEquals("eth0", first.getKeys().get(0).getStringValue());
    }
}
