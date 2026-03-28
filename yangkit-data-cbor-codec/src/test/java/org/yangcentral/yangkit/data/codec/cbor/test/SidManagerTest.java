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

import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.codec.cbor.SidManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for SidManager functionality.
 * Tests SID generation, registration, and lookup.
 * 
 * @author Yangkit Team
 */
public class SidManagerTest {
    
    /**
     * Test basic SID generation.
     */
    @Test
    public void testSidGeneration() {
        SidManager sidManager = new SidManager();
        
        QName qName1 = new QName("http://example.com/test", "container-name");
        QName qName2 = new QName("http://example.com/test", "leaf-name");
        
        Long sid1 = sidManager.getSid(qName1);
        Long sid2 = sidManager.getSid(qName2);
        
        assertNotNull(sid1);
        assertNotNull(sid2);
        
        // SIDs should be unique for different nodes
        assertNotEquals(sid1, sid2);
        
        // SIDs should be in valid range
        assertTrue(sid1 >= SidManager.SID_RANGE_START);
        assertTrue(sid1 <= SidManager.SID_RANGE_END);
        assertTrue(sid2 >= SidManager.SID_RANGE_START);
        assertTrue(sid2 <= SidManager.SID_RANGE_END);
    }
    
    /**
     * Test SID caching and consistency.
     */
    @Test
    public void testSidCaching() {
        SidManager sidManager = new SidManager();
        
        QName qName = new QName("http://example.com/test", "test-node");
        
        Long sid1 = sidManager.getSid(qName);
        Long sid2 = sidManager.getSid(qName);
        
        // Same QName should return same SID
        assertEquals(sid1, sid2);
    }
    
    /**
     * Test module SID range registration.
     */
    @Test
    public void testModuleRegistration() {
        SidManager sidManager = new SidManager();
        
        // Register a module with specific SID range
        String namespace = "http://example.com/my-module";
        long baseSid = 10000;
        int size = 100;
        
        sidManager.registerModule(namespace, baseSid, size);
        
        // Create a node in this module
        QName qName = new QName(namespace, "my-container");
        
        Long sid = sidManager.getSid(qName);
        
        assertNotNull(sid);
        
        // Debug output
        System.out.println("Generated SID: " + sid + ", Base: " + baseSid + ", Size: " + size);
        
        // SID should be within registered range
        assertTrue(sid >= baseSid, "SID (" + sid + ") should be >= baseSid (" + baseSid + ")");
        assertTrue(sid < baseSid + size, "SID (" + sid + ") should be < baseSid+size (" + (baseSid + size) + ")");
    }
    
    /**
     * Test reverse lookup (SID to QName).
     */
    @Test
    public void testReverseLookup() {
        SidManager sidManager = new SidManager();
        
        QName originalQName = new QName("http://example.com/test", "my-leaf");
        Long sid = sidManager.getSid(originalQName);
        
        QName resolvedQName = sidManager.getQName(sid);
        
        assertNotNull(resolvedQName);
        assertEquals(originalQName, resolvedQName);
    }
    
    /**
     * Test .sid file loading.
     */
    @Test
    public void testSidFileLoading() {
        SidManager sidManager = new SidManager();
        
        // Sample .sid file content
        String sidFileContent = 
            "# Example SID file\n" +
            "namespace: http://example.com/test\n" +
            "sid-range: 10000-10099\n" +
            "assignment: container1 10001\n" +
            "assignment: leaf1 10002\n" +
            "assignment: leaf2 10003\n";
        
        sidManager.loadSidFile(sidFileContent);
        
        // Test loaded assignments
        QName container1QName = new QName("http://example.com/test", "container1");
        Long container1Sid = sidManager.getSid(container1QName);
        
        assertEquals(Long.valueOf(10001), container1Sid);
        
        // Test reverse lookup
        QName resolved = sidManager.getQName(10002L);
        assertNotNull(resolved);
        assertEquals("leaf1", resolved.getLocalName());
        assertEquals("http://example.com/test", resolved.getNamespace().toString());
    }
    
    /**
     * Test SID encoding enabled check.
     */
    @Test
    public void testSidEncodingEnabled() {
        SidManager sidManager1 = new SidManager();
        assertFalse(sidManager1.isSidEncodingEnabled());
        
        SidManager sidManager2 = new SidManager();
        sidManager2.registerModule("http://example.com/test", 10000, 100);
        assertTrue(sidManager2.isSidEncodingEnabled());
        
        SidManager sidManager3 = new SidManager();
        QName qName = new QName("http://example.com/test", "node");
        sidManager3.getSid(qName);
        assertTrue(sidManager3.isSidEncodingEnabled());
    }
    
    /**
     * Test multiple modules with different SID ranges.
     */
    @Test
    public void testMultipleModules() {
        SidManager sidManager = new SidManager();
        
        // Register two modules with different SID ranges
        sidManager.registerModule("http://example.com/module1", 10000, 100);
        sidManager.registerModule("http://example.com/module2", 20000, 100);
        
        QName qName1 = new QName("http://example.com/module1", "node");
        QName qName2 = new QName("http://example.com/module2", "node");
        
        Long sid1 = sidManager.getSid(qName1);
        Long sid2 = sidManager.getSid(qName2);
        
        assertNotNull(sid1);
        assertNotNull(sid2);
        
        // SIDs from different modules should be in different ranges
        assertTrue(sid1 >= 10000 && sid1 < 10100);
        assertTrue(sid2 >= 20000 && sid2 < 20100);
        
        // Even with same local name, SIDs should be different
        assertNotEquals(sid1, sid2);
    }
}
