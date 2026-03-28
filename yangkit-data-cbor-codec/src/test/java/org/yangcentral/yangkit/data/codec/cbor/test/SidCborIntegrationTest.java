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
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.codec.cbor.*;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SID-based CBOR encoding/decoding using parsed YANG models.
 * Tests RFC 9254 Section 5 - SID-based encoding functionality.
 * 
 * @author Yangkit Team
 */
public class SidCborIntegrationTest {
    
    private static YangSchemaContext schemaContext;
    private static org.yangcentral.yangkit.model.api.stmt.Module testModule;
    
    static {
        try {
            // Load and parse the test YANG module
            URL yangUrl = SidCborIntegrationTest.class.getClassLoader().getResource("test-module.yang");
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
     * Helper method to create a Leaf with proper context for testing.
     */
    private Leaf createTestLeaf(String name, String namespace) {
        Leaf leaf = (Leaf) testModule.getDataDefChild(name);
        if (leaf == null) {
            // Fallback to test-string if specific leaf not found
            leaf = (Leaf) testModule.getDataDefChild("test-string");
        }
        return leaf;
    }
    
    /**
     * Helper method to create a Container with proper context for testing.
     */
    private Container createTestContainer(String name, String namespace) {
        Container container = (Container) testModule.getDataDefChild(name);
        if (container == null) {
            // Fallback to test-container if specific container not found
            container = (Container) testModule.getDataDefChild("test-container");
        }
        return container;
    }
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * Test SID-based container encoding and decoding.
     */
    @Test
    public void testSidContainerEncoding() throws Exception {
        // Create SID manager and register module
        SidManager sidManager = new SidManager();
        String namespace = "http://example.com/test-module";
        sidManager.registerModule(namespace, 10000, 100);
        
        // Get container schema from parsed YANG model
        Container container = (Container) testModule.getDataDefChild("test-container");
        assertNotNull(container, "test-container should exist in the test module");
        
        // Get child leaf schemas from the container
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");
        assertNotNull(nameLeaf, "name leaf should exist in test-container");
        assertNotNull(valueLeaf, "value leaf should exist in test-container");
        
        // Create container data
        ContainerDataImpl containerData = new ContainerDataImpl(container);
        
        // Add leaf data with proper values
        LeafData<String> nameData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(nameLeaf, "test-name");
        LeafData<Integer> valueData = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(valueLeaf, "42");
        
        containerData.addDataChild(nameData);
        containerData.addDataChild(valueData);
        
        // Serialize with SID encoding
        SidContainerDataCborCodec codec = new SidContainerDataCborCodec(
            container, sidManager);
        
        byte[] cborBytes = codec.serialize(containerData);
        
        assertNotNull(cborBytes);
        assertTrue(cborBytes.length > 0);
        
        // Deserialize
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ContainerData deserialized = codec.deserialize(cborBytes, validatorResultBuilder);
        
        assertNotNull(deserialized);
        
        // Verify the data can be read back
        System.out.println("SID-based encoding successful!");
        System.out.println("CBOR encoded size: " + cborBytes.length + " bytes");
    }
    
    /**
     * Test name-based vs SID-based encoding comparison.
     */
    @Test
    public void testEncodingComparison() throws Exception {
        // Create SID manager
        SidManager sidManager = new SidManager();
        String namespace = "http://example.com/test-module";
        sidManager.registerModule(namespace, 10000, 100);
        
        // Get container schema from parsed YANG model
        Container container = (Container) testModule.getDataDefChild("test-container");
        assertNotNull(container, "test-container should exist in the test module");
        
        // Get child leaf schema
        Leaf valueLeaf = (Leaf) container.getDataDefChild("value");
        assertNotNull(valueLeaf, "value leaf should exist in test-container");
        
        // Create container data with one leaf
        ContainerDataImpl containerData = new ContainerDataImpl(container);
        LeafData<Integer> valueData = (LeafData<Integer>) YangDataBuilderFactory.getBuilder()
            .getYangData(valueLeaf, "100");
        containerData.addDataChild(valueData);
        
        // Encode with name-based (existing)
        ContainerDataCborCodec nameBasedCodec = new ContainerDataCborCodec(container);
        byte[] nameBasedBytes = nameBasedCodec.serialize(containerData);
        
        // Encode with SID-based
        SidContainerDataCborCodec sidBasedCodec = 
            new SidContainerDataCborCodec(container, sidManager);
        byte[] sidBasedBytes = sidBasedCodec.serialize(containerData);
        
        assertNotNull(nameBasedBytes);
        assertNotNull(sidBasedBytes);
        
        // Print sizes for comparison
        System.out.println("Name-based encoding size: " + nameBasedBytes.length + " bytes");
        System.out.println("SID-based encoding size: " + sidBasedBytes.length + " bytes");
        
        // In theory, SID-based should be more compact for repeated structures
        // However, for single items, overhead might make it similar or larger
        
        // Both should deserialize successfully
        ValidatorResultBuilder builder = new ValidatorResultBuilder();
        
        ContainerData nameBasedResult = nameBasedCodec.deserialize(nameBasedBytes, builder);
        ContainerData sidBasedResult = sidBasedCodec.deserialize(sidBasedBytes, builder);
        
        assertNotNull(nameBasedResult);
        assertNotNull(sidBasedResult);
    }
    
    /**
     * Test SID generation and consistency.
     */
    @Test
    public void testSidConsistency() throws Exception {
        SidManager sidManager = new SidManager();
        String namespace = "http://example.com/module";
        sidManager.registerModule(namespace, 20000, 200);
        
        // Generate SIDs for multiple nodes
        QName qName1 = new QName(namespace, "node1");
        QName qName2 = new QName(namespace, "node2");
        QName qName3 = new QName(namespace, "node1"); // Same as qName1
        
        Long sid1 = sidManager.getSid(qName1);
        Long sid2 = sidManager.getSid(qName2);
        Long sid3 = sidManager.getSid(qName3);
        
        assertNotNull(sid1);
        assertNotNull(sid2);
        assertNotNull(sid3);
        
        // Same node should get same SID
        assertEquals(sid1, sid3);
        
        // Different nodes should get different SIDs
        assertNotEquals(sid1, sid2);
        
        // Verify reverse lookup
        assertEquals(qName1, sidManager.getQName(sid1));
        assertEquals(qName2, sidManager.getQName(sid2));
        assertEquals(qName3, sidManager.getQName(sid3));
    }
    
    /**
     * Test .sid file format loading and usage.
     */
    @Test
    public void testSidFileFormat() throws Exception {
        // Realistic .sid file content example
        String sidFileContent = 
            "# YANG Module SID Assignment\n" +
            "# Generated for example-module@1.0.0\n" +
            "\n" +
            "module: example-module\n" +
            "namespace: http://example.com/example-module\n" +
            "revision: 2024-01-01\n" +
            "sid-range: 30000-30099\n" +
            "\n" +
            "# Data nodes\n" +
            "assignment: configuration 30001\n" +
            "assignment: system-settings 30002\n" +
            "assignment: hostname 30003\n" +
            "assignment: ip-address 30004\n" +
            "assignment: port 30005\n" +
            "\n" +
            "# State nodes\n" +
            "assignment: status 30010\n" +
            "assignment: uptime 30011\n" +
            "assignment: version 30012\n";
        
        SidManager sidManager = new SidManager();
        sidManager.loadSidFile(sidFileContent);
        
        // Verify loaded SIDs
        QName configQName = new QName("http://example.com/example-module", "configuration");
        Long configSid = sidManager.getSid(configQName);
        
        assertEquals(Long.valueOf(30001), configSid);
        
        // Verify all assigned SIDs are correct
        assertEquals(Long.valueOf(30003), 
            sidManager.getSid(new QName("http://example.com/example-module", "hostname")));
        assertEquals(Long.valueOf(30004), 
            sidManager.getSid(new QName("http://example.com/example-module", "ip-address")));
        assertEquals(Long.valueOf(30010), 
            sidManager.getSid(new QName("http://example.com/example-module", "status")));
        
        // Verify reverse lookups
        assertEquals("system-settings", 
            sidManager.getQName(30002L).getLocalName());
        assertEquals("uptime", 
            sidManager.getQName(30011L).getLocalName());
    }
    
    /**
     * Test fallback to name-based encoding when SID not available.
     */
    @Test
    public void testFallbackToNameBased() throws Exception {
        // Create SID manager but don't register any module
        SidManager sidManager = new SidManager();
        
        // Get container schema from parsed YANG model
        Container container = (Container) testModule.getDataDefChild("test-container");
        assertNotNull(container, "test-container should exist in the test module");
        
        // Get child leaf schema
        Leaf nameLeaf = (Leaf) container.getDataDefChild("name");
        assertNotNull(nameLeaf, "name leaf should exist in test-container");
        
        // Create container data
        ContainerDataImpl containerData = new ContainerDataImpl(container);
        LeafData<String> nameData = (LeafData<String>) YangDataBuilderFactory.getBuilder()
            .getYangData(nameLeaf, "fallback-test");
        containerData.addDataChild(nameData);
        
        // Create codec with SID support but no SID assignments
        SidContainerDataCborCodec codec = new SidContainerDataCborCodec(
            container, sidManager);
        
        // Should still work (fallback to name-based or default SID generation)
        byte[] cborBytes = codec.serialize(containerData);
        
        assertNotNull(cborBytes);
        
        ValidatorResultBuilder builder = new ValidatorResultBuilder();
        ContainerData result = codec.deserialize(cborBytes, builder);
        
        assertNotNull(result);
    }
}
