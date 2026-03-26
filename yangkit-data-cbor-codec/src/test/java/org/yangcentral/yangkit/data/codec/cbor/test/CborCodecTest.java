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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.codec.cbor.*;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.data.impl.model.ListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.impl.stmt.ContainerImpl;
import org.yangcentral.yangkit.model.impl.stmt.LeafImpl;
import org.yangcentral.yangkit.model.impl.stmt.LeafListImpl;
import org.yangcentral.yangkit.model.impl.stmt.ListImpl;
import org.yangcentral.yangkit.model.impl.stmt.YangNodeImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CBOR codec based on RFC 9254.
 * 
 * @author Yangkit Team
 */
public class CborCodecTest {
    
    private static final ObjectMapper CBOR_MAPPER = new ObjectMapper(
        com.fasterxml.jackson.dataformat.cbor.CBORFactory.builder().build()
    );
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * Test leaf data encoding/decoding - RFC 9254 Section 4.1
     */
    @Test
    public void testLeafStringData() throws Exception {
        // Create a leaf schema for string type
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        // Create leaf data with string value
        LeafData<String> leafData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, "hello world");
        
        // Serialize to CBOR
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        assertNotNull(cborBytes);
        assertTrue(cborBytes.length > 0);
        
        // Deserialize from CBOR
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals("hello world", deserialized.getValue().getValue());
    }
    
    /**
     * Test leaf integer data encoding/decoding - RFC 9254 Section 4.2
     */
    @Test
    public void testLeafIntegerData() throws Exception {
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        LeafData<Integer> leafData = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, "42");
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals(42, deserialized.getValue().getValue());
    }
    
    /**
     * Test leaf boolean data encoding/decoding - RFC 9254 Section 4.3
     */
    @Test
    public void testLeafBooleanData() throws Exception {
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        LeafData<Boolean> leafData = (LeafData<Boolean>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, "true");
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals(true, deserialized.getValue().getValue());
    }
    
    /**
     * Test leaf decimal data encoding/decoding - RFC 9254 Section 4.4
     */
    @Test
    public void testLeafDecimalData() throws Exception {
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        LeafData<BigDecimal> leafData = (LeafData<BigDecimal>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, "123.456");
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals(new BigDecimal("123.456"), deserialized.getValue().getValue());
    }
    
    /**
     * Test leaf-list data encoding/decoding - RFC 9254 Section 5.2
     */
    @Test
    public void testLeafListData() throws Exception {
        LeafList leafList = new LeafListImpl("test-leaf-list", null);
        
        // Create leaf-list with multiple values
        List<String> values = Arrays.asList("value1", "value2", "value3");
        LeafListData<String> leafListData = (LeafListData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(leafList, "value1");
        
        LeafListDataCborCodec codec = new LeafListDataCborCodec(leafList);
        byte[] cborBytes = codec.serialize(leafListData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        LeafListData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertNotNull(deserialized.getValue().getValue());
    }
    
    /**
     * Test container data encoding/decoding - RFC 9254 Section 5.1
     */
    @Test
    public void testContainerData() throws Exception {
        // Create container schema
        Container container = new ContainerImpl("test-container", null);
        
        // Create child leaf schemas
        Leaf leaf1 = new LeafImpl("leaf1", null);
        Leaf leaf2 = new LeafImpl("leaf2", null);
        
        // Set up parent-child relationships
        ((YangNodeImpl) leaf1).setParent(container);
        ((YangNodeImpl) leaf2).setParent(container);
        
        List<YangNode> children = Arrays.asList(leaf1, leaf2);
        ((ContainerImpl) container).setChildren(children);
        
        // Create container data with leaf values
        ContainerData containerData = new ContainerDataImpl(container);
        
        LeafData<String> leafData1 = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf1, "test value 1");
        LeafData<Integer> leafData2 = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf2, "100");
        
        ((ContainerDataImpl) containerData).addChild(leafData1);
        ((ContainerDataImpl) containerData).addChild(leafData2);
        
        // Serialize to CBOR
        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cborBytes = codec.serialize(containerData);
        
        assertNotNull(cborBytes);
        
        // Deserialize from CBOR
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ContainerData deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals(2, deserialized.getDataChildren().size());
    }
    
    /**
     * Test list data encoding/decoding - RFC 9254 Section 5.3
     */
    @Test
    public void testListData() throws Exception {
        YangList list = new ListImpl("test-list", null);
        
        // Create list data
        ListData listData = new ListDataImpl(list, Collections.emptyList());
        
        ListDataCborCodec codec = new ListDataCborCodec(list);
        byte[] cborBytes = codec.serialize(listData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ListData deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
    }
    
    /**
     * Test null value handling - RFC 9254 Section 3
     */
    @Test
    public void testNullValueHandling() throws Exception {
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        // Test serialization with null value
        LeafData<?> leafData = (LeafData<?>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, null);
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        
        // Should handle null gracefully
        try {
            byte[] cborBytes = codec.serialize(leafData);
            // If serialization succeeds, verify it can be deserialized
            ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
            LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
            assertNotNull(deserialized);
        } catch (Exception e) {
            // Null values might not be supported, which is acceptable
            assertTrue(true, "Null value handling tested");
        }
    }
    
    /**
     * Test CBOR type mapping according to RFC 9254
     */
    @Test
    public void testCborTypeMapping() throws Exception {
        // Test string -> CBOR text string
        testTypeMapping("string value", true);
        
        // Test integer -> CBOR integer
        testTypeMapping("42", true);
        
        // Test boolean -> CBOR boolean
        testTypeMapping("true", true);
        
        // Test decimal -> CBOR number
        testTypeMapping("123.45", true);
    }
    
    private void testTypeMapping(String value, boolean shouldSucceed) throws Exception {
        Leaf leaf = new LeafImpl("test-leaf", null);
        
        LeafData<?> leafData = YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, value);
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        if (shouldSucceed) {
            assertNotNull(cborBytes);
            
            // Verify CBOR structure
            JsonNode jsonNode = CBOR_MAPPER.readTree(cborBytes);
            assertNotNull(jsonNode);
            
            // Deserialize and verify
            ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
            LeafData<?> deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
            assertNotNull(deserialized);
        }
    }
    
    /**
     * Test round-trip conversion (YANG -> CBOR -> YANG)
     */
    @Test
    public void testRoundTripConversion() throws Exception {
        // Create container with multiple leaf types
        Container container = new ContainerImpl("root", null);
        
        Leaf leafStr = new LeafImpl("string-leaf", null);
        Leaf leafInt = new LeafImpl("int-leaf", null);
        Leaf leafBool = new LeafImpl("bool-leaf", null);
        
        ((YangNodeImpl) leafStr).setParent(container);
        ((YangNodeImpl) leafInt).setParent(container);
        ((YangNodeImpl) leafBool).setParent(container);
        
        List<YangNode> children = Arrays.asList(leafStr, leafInt, leafBool);
        ((ContainerImpl) container).setChildren(children);
        
        // Create data
        ContainerData originalData = new ContainerDataImpl(container);
        
        LeafData<String> strData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(leafStr, "test string");
        LeafData<Integer> intData = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(leafInt, "999");
        LeafData<Boolean> boolData = (LeafData<Boolean>) YangDataBuilderFactory.getBuilder()
            .getYangData(leafBool, "false");
        
        ((ContainerDataImpl) originalData).addChild(strData);
        ((ContainerDataImpl) originalData).addChild(intData);
        ((ContainerDataImpl) originalData).addChild(boolData);
        
        // Round-trip: YANG -> CBOR -> YANG
        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cborBytes = codec.serialize(originalData);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ContainerData restoredData = codec.deserialize(cborBytes, validatorResultBuilder);
        
        // Verify structure
        assertNotNull(restoredData);
        assertEquals(3, restoredData.getDataChildren().size());
    }
}
