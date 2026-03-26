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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Leaf;

/**
 * Example demonstrating SID-based CBOR encoding.
 * 
 * @author Yangkit Team
 */
public class SidExample {
    
    /**
     * Demonstrates basic SID usage.
     */
    public static void demonstrateBasicSidUsage() {
        System.out.println("=== Basic SID Manager Usage ===\n");
        
        // Create SID manager
        SidManager sidManager = new SidManager();
        
        // Register a module with SID range
        String namespace = "http://example.com/network";
        long baseSid = 10000;
        int size = 100;
        sidManager.registerModule(namespace, baseSid, size);
        
        // Create sample QNames
        QName containerQName = new QName(namespace, "interface");
        QName leafQName = new QName(namespace, "name");
        
        // Get SIDs for nodes
        Long containerSid = sidManager.getSid(containerQName);
        Long leafSid = sidManager.getSid(leafQName);
        
        System.out.println("Container 'interface' SID: " + containerSid);
        System.out.println("Leaf 'name' SID: " + leafSid);
        
        // Reverse lookup
        QName resolvedContainer = sidManager.getQName(containerSid);
        System.out.println("SID " + containerSid + " resolves to: " + 
                          (resolvedContainer != null ? resolvedContainer.getLocalName() : "unknown"));
        
        System.out.println();
    }
    
    /**
     * Demonstrates SID encoding/decoding.
     * Note: This is a simplified example. Full implementation requires actual schema nodes.
     */
    public static void demonstrateSidEncoding() {
        System.out.println("=== SID Encoding/Decoding ===\n");
        
        try {
            // Create SID manager
            SidManager sidManager = new SidManager();
            sidManager.registerModule("http://example.com/test", 10000, 100);
            
            // In a real scenario, you would have actual Container schema from YANG parsing
            // For this example, we'll show the API usage pattern
            
            // Create sample container data (simplified)
            // Container schemaNode = ... // From parsed YANG model
            // ContainerData containerData = new ContainerDataImpl(schemaNode);
            // LeafData leafData = new LeafDataImpl<>(someLeaf);
            // containerData.addChild(leafData);
            
            System.out.println("SID encoding workflow:");
            System.out.println("1. Create and configure SidManager");
            System.out.println("2. Create SidContainerDataCborCodec with SidManager");
            System.out.println("3. Call codec.serialize(containerData) to encode with SIDs");
            System.out.println("4. Call codec.deserialize(cborBytes, validator) to decode");
            System.out.println();
            
            // Example using SidEncoder directly
            System.out.println("Using SidEncoder utility:");
            System.out.println("- SidEncoder.encodeWithSid(container, sidManager)");
            System.out.println("- SidEncoder.decodeWithSid(jsonNode, sidManager)");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error during SID encoding: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrates .sid file format.
     */
    public static void demonstrateSidFileFormat() {
        System.out.println("=== .sid File Format Example ===\n");
        
        String exampleSidFile = 
            "# SID assignment for example-module\n" +
            "namespace: http://example.com/network\n" +
            "sid-range: 10000-10099\n" +
            "\n" +
            "# Explicit assignments\n" +
            "assignment: interface 10001\n" +
            "assignment: name 10002\n" +
            "assignment: mtu 10003\n" +
            "assignment: enabled 10004\n";
        
        System.out.println("Example .sid file content:");
        System.out.println("-----------------------------------");
        System.out.println(exampleSidFile);
        System.out.println("-----------------------------------\n");
        
        // Load it programmatically
        SidManager sidManager = new SidManager();
        sidManager.loadSidFile(exampleSidFile);
        
        // Check loaded assignments
        QName interfaceQName = new QName("http://example.com/network", "interface");
        Long interfaceSid = sidManager.getSid(interfaceQName);
        System.out.println("Loaded SID for 'interface': " + interfaceSid);
        System.out.println();
    }
    
    /**
     * Demonstrates high-level API.
     */
    public static void demonstrateHighLevelApi() {
        System.out.println("=== High-Level SID API ===\n");
        
        System.out.println("SidCborEncoder provides convenient methods:");
        System.out.println();
        System.out.println("// Encode container to CBOR with SID tag");
        System.out.println("byte[] cborBytes = SidCborEncoder.encodeToCbor(containerData, sidManager);");
        System.out.println();
        System.out.println("// Decode from CBOR with automatic tag handling");
        System.out.println("JsonNode result = SidCborEncoder.decodeFromCbor(cborBytes, sidManager);");
        System.out.println();
        
        System.out.println("Benefits:");
        System.out.println("✓ Automatic CBOR tag application (60000-60999 range)");
        System.out.println("✓ Simplified API for common use cases");
        System.out.println("✓ RFC 9254 Section 5 compliant");
        System.out.println();
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        System.out.println("YANGKIT SID-based CBOR Encoding Examples");
        System.out.println("=========================================\n");
        
        try {
            demonstrateBasicSidUsage();
            demonstrateSidFileFormat();
            demonstrateSidEncoding();
            demonstrateHighLevelApi();
            
            System.out.println("=========================================");
            System.out.println("All examples completed successfully!");
            System.out.println("See SID_SUPPORT.md for detailed documentation.");
            
        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
