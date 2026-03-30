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
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end round-trip tests: builds a YANG data tree, encodes to CBOR,
 * decodes back and verifies structural and value fidelity.
 *
 * @author Yangkit Team
 */
public class CborRoundTripTest {

    private static org.yangcentral.yangkit.model.api.stmt.Module testModule;

    @BeforeAll
    static void setup() throws Exception {
        URL yangUrl = CborRoundTripTest.class.getClassLoader().getResource("test-module.yang");
        assertNotNull(yangUrl);
        YangSchemaContext ctx = YangYinParser.parse(yangUrl.getPath());
        assertTrue(ctx.validate().isOk());
        testModule = ctx.getLatestModule("test-module")
                .orElseThrow(() -> new AssertionError("test-module not found in schema ctx"));
    }

    // ------------------------------------------------------------------
    // Simple container with leaf children
    // ------------------------------------------------------------------

    @Test
    void testContainerRoundTrip() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        assertNotNull(container);
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "my-node"));
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(valueLeaf, "99"));

        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cbor = codec.serialize(data);
        assertTrue(cbor.length > 0);

        ContainerData decoded = codec.deserialize(cbor, new ValidatorResultBuilder());
        assertNotNull(decoded);

        List<YangData<?>> children = decoded.getDataChildren();
        assertEquals(2, children.size());

        String nameVal = null, valueVal = null;
        for (YangData<?> child : children) {
            if ("name".equals(child.getQName().getLocalName())) {
                nameVal = ((LeafData<?>) child).getStringValue();
            } else if ("value".equals(child.getQName().getLocalName())) {
                valueVal = ((LeafData<?>) child).getStringValue();
            }
        }
        assertEquals("my-node", nameVal);
        assertEquals("99", valueVal);
    }

    // ------------------------------------------------------------------
    // Network container with multiple interface list entries
    // ------------------------------------------------------------------

    @Test
    void testNestedContainerWithList() throws Exception {
        Container network = (Container) testModule.getDataDefChild("network");
        assertNotNull(network);
        YangList ifaceList = (YangList) network.getDataDefChild("interface");
        Leaf nameLeaf = (Leaf) ifaceList.getDataDefChild("name");
        Leaf enabledLeaf = (Leaf) ifaceList.getDataDefChild("enabled");
        Leaf mtuLeaf = (Leaf) ifaceList.getDataDefChild("mtu");

        ContainerDataImpl networkData = new ContainerDataImpl(network);
        networkData.setQName(network.getIdentifier());

        // 3 interfaces
        Object[][] entries = {
            {"lo",   "true",  "65536"},
            {"eth0", "true",  "1500"},
            {"eth1", "false", "9000"},
        };
        for (Object[] row : entries) {
            LeafData<?> keyData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(nameLeaf, (String) row[0]);
            List<LeafData> keys = new ArrayList<>();
            keys.add(keyData);
            @SuppressWarnings("unchecked")
            ListData iface = (ListData) YangDataBuilderFactory.getBuilder()
                    .getYangData(ifaceList, keys);
            iface.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(enabledLeaf, (String) row[1]));
            iface.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                    .getYangData(mtuLeaf, (String) row[2]));
            networkData.addChild(iface);
        }

        ContainerDataCborCodec codec = new ContainerDataCborCodec(network);
        byte[] cbor = codec.serialize(networkData);
        assertTrue(cbor.length > 0);

        ContainerData decoded = codec.deserialize(cbor, new ValidatorResultBuilder());
        assertNotNull(decoded);

        List<YangData<?>> children = decoded.getDataChildren();
        long listCount = children.stream().filter(c -> c instanceof ListData).count();
        assertEquals(3, listCount, "All 3 interface entries must survive round-trip");
    }

    // ------------------------------------------------------------------
    // CBOR is more compact than equivalent JSON for structured data
    // ------------------------------------------------------------------

    @Test
    void testCborIsSmallerThanJson() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "example-name"));
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(valueLeaf, "42"));

        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cbor = codec.serialize(data);

        // Rough JSON equivalent for comparison
        String json = "{\"name\":\"example-name\",\"value\":42}";
        assertTrue(cbor.length <= json.length(),
                "CBOR (" + cbor.length + " bytes) should not exceed JSON (" + json.length() + " bytes)");
    }

    // ------------------------------------------------------------------
    // Decimal64 precision is preserved across CBOR round-trip
    // ------------------------------------------------------------------

    @Test
    void testDecimal64PrecisionPreserved() throws Exception {
        Leaf decLeaf = (Leaf) testModule.getDataDefChild("test-decimal");
        assertNotNull(decLeaf);

        String value = "3.141592";
        LeafData<?> data = (LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(decLeaf, value);

        org.yangcentral.yangkit.data.codec.cbor.LeafDataCborCodec codec =
                new org.yangcentral.yangkit.data.codec.cbor.LeafDataCborCodec(decLeaf);
        byte[] cbor = codec.serialize(data);
        LeafData<?> decoded = codec.deserialize(cbor, new ValidatorResultBuilder());

        assertNotNull(decoded);
        assertEquals(new BigDecimal(value), new BigDecimal(decoded.getStringValue()),
                "Decimal64 precision must be preserved");
    }

    // ------------------------------------------------------------------
    // Verify CBOR output starts with correct major type for containers
    // ------------------------------------------------------------------

    @Test
    void testContainerCborStartsWithMap() throws Exception {
        Container container = (Container) testModule.getDataDefChild("test-container");
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");

        ContainerDataImpl data = new ContainerDataImpl(container);
        data.setQName(container.getIdentifier());
        data.addChild((LeafData<?>) YangDataBuilderFactory.getBuilder()
                .getYangData(nameLeaf, "test"));

        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cbor = codec.serialize(data);

        // CBOR map major type = 0xA0..0xBF (major type 5)
        int firstByte = cbor[0] & 0xFF;
        assertTrue((firstByte & 0xE0) == 0xA0,
                "Container should be encoded as CBOR map (major type 5), got: 0x"
                        + Integer.toHexString(firstByte));
    }
}
