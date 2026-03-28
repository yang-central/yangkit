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

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.codec.cbor.*;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafListDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CBOR codec based on RFC 9254 using parsed YANG models.
 * Tests basic encoding/decoding functionality for various YANG data types.
 * 
 * @author Yangkit Team
 */
public class CborBasicTest {
    
    private static YangSchemaContext schemaContext;
    private static org.yangcentral.yangkit.model.api.stmt.Module testModule;
    
    static {
        try {
            // Load and parse the test YANG module
            URL yangUrl = CborBasicTest.class.getClassLoader().getResource("test-module.yang");
            if (yangUrl == null) {
                throw new RuntimeException("Cannot find test-module.yang in classpath");
            }
            String yangDir = yangUrl.getPath();
            schemaContext = YangYinParser.parse(yangDir);
            
            // Validate the schema context to initialize YangContext
            org.yangcentral.yangkit.common.api.validate.ValidatorResult validatorResult = schemaContext.validate();
            if (!validatorResult.isOk()) {
                throw new RuntimeException("Failed to validate schema context: " + validatorResult);
            }
            
            // Get the test module from schema context using getLatestModule
            testModule = schemaContext.getLatestModule("test-module")
                .orElseThrow(() -> new RuntimeException("Failed to find test-module in schema context"));
        } catch (DocumentException | IOException | YangParserException e) {
            throw new RuntimeException("Failed to load test YANG module", e);
        }
    }
    
    /**
     * Get a leaf schema from the test module by name.
     */
    private Leaf getLeaf(String name) {
        return (Leaf) testModule.getDataDefChild(name);
    }
    
    /**
     * Get a container schema from the test module by name.
     */
    private Container getContainer(String name) {
        return (Container) testModule.getDataDefChild(name);
    }
    
    /**
     * Get a leaf-list schema from the test module by name.
     */
    private LeafList getLeafList(String name) {
        return (LeafList) testModule.getDataDefChild(name);
    }
    
    /**
     * Test leaf string data encoding/decoding - RFC 9254 Section 4.1
     */
    @Test
    public void testLeafStringData() throws Exception {
        Leaf leaf = getLeaf("test-string");
        assertNotNull(leaf, "test-string leaf should exist in the test module");
        
        LeafData<String> leafData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(leaf, "hello world");
        
        LeafDataCborCodec codec = new LeafDataCborCodec(leaf);
        byte[] cborBytes = codec.serialize(leafData);
        
        assertNotNull(cborBytes);
        assertTrue(cborBytes.length > 0);
        
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
        Leaf leaf = getLeaf("test-integer");
        assertNotNull(leaf, "test-integer leaf should exist in the test module");
        
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
        Leaf leaf = getLeaf("test-boolean");
        assertNotNull(leaf, "test-boolean leaf should exist in the test module");
        
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
        Leaf leaf = getLeaf("test-decimal");
        assertNotNull(leaf, "test-decimal leaf should exist in the test module");
        
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
     * Test container data encoding/decoding - RFC 9254 Section 5.1
     */
    @Test
    public void testContainerData() throws Exception {
        Container container = getContainer("test-container");
        assertNotNull(container, "test-container should exist in the test module");
        
        ContainerData containerData = new ContainerDataImpl(container);
        
        // Create child data using actual schema nodes from the model
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");
        
        LeafData<String> nameData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(nameLeaf, "test value 1");
        LeafData<Integer> valueData = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(valueLeaf, "100");
        
        ((ContainerDataImpl) containerData).addChild(nameData);
        ((ContainerDataImpl) containerData).addChild(valueData);
        
        ContainerDataCborCodec codec = new ContainerDataCborCodec(container);
        byte[] cborBytes = codec.serialize(containerData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ContainerData deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        assertEquals(2, deserialized.getDataChildren().size());
    }
    
    /**
     * Test leaf-list data encoding/decoding - RFC 9254 Section 5.2
     */
    @Test
    public void testLeafListData() throws Exception {
        LeafList leafList = getLeafList("test-leaf-list");
        assertNotNull(leafList, "test-leaf-list should exist in the test module");
        
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
}
