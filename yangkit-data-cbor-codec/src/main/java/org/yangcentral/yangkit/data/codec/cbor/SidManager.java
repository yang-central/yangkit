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

import org.yangcentral.yangkit.common.api.QName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema Item Identifier (SID) manager.
 * Manages the mapping between YANG schema nodes and their SIDs.
 * 
 * <p>SID is a unique 64-bit integer identifier assigned to YANG schema nodes
 * as defined in RFC 9254 Section 5.</p>
 * 
 * @author Yangkit Team
 */
public class SidManager {
    
    /**
     * CBOR tag for SID-based encoding (RFC 9254)
     */
    public static final int CBOR_TAG_SID = 60000;
    
    /**
     * SID range for YANG modules (60000-60999)
     */
    public static final int SID_RANGE_START = 60000;
    public static final int SID_RANGE_END = 60999;
    
    // Cache for QName to SID mapping
    private final Map<QName, Long> qnameToSidMap = new ConcurrentHashMap<>();
    
    // Cache for SID to QName mapping
    private final Map<Long, QName> sidToQnameMap = new ConcurrentHashMap<>();
    
    // Module namespace to SID range mapping
    private final Map<String, SidRange> moduleSidRanges = new HashMap<>();
    
    /**
     * Registers a YANG module with its SID range.
     * 
     * @param namespace the module namespace
     * @param baseSid the base SID for this module
     * @param size the number of SIDs allocated for this module
     */
    public void registerModule(String namespace, long baseSid, int size) {
        SidRange range = new SidRange(namespace, baseSid, size);
        moduleSidRanges.put(namespace, range);
    }
    
    /**
     * Gets or generates the SID for a YANG node.
     * 
     * @param qName the qualified name of the node
     * @return the SID value, or null if not found
     */
    public Long getSid(QName qName) {
        // Check cache first
        Long sid = qnameToSidMap.get(qName);
        if (sid != null) {
            return sid;
        }
        
        // Try to generate SID from module SID range
        // Use toString() to ensure we're comparing strings properly
        SidRange range = moduleSidRanges.get(qName.getNamespace().toString());
        if (range != null) {
            // Generate SID based on node name hash
            int index = generateNodeIndex(qName.getLocalName());
            sid = range.getBaseSid() + (index % range.getSize());
            
            // Cache the mapping
            qnameToSidMap.put(qName, sid);
            sidToQnameMap.put(sid, qName);
            
            return sid;
        }
        
        // Use default SID generation algorithm
        sid = generateDefaultSid(qName);
        if (sid != null) {
            qnameToSidMap.put(qName, sid);
            sidToQnameMap.put(sid, qName);
        }
        
        return sid;
    }
    
    /**
     * Gets the QName for a given SID.
     * 
     * @param sid the SID value
     * @return the QName, or null if not found
     */
    public QName getQName(Long sid) {
        return sidToQnameMap.get(sid);
    }
    
    /**
     * Loads SID assignments from a .sid file.
     * 
     * @param sidFileContent the content of the .sid file
     */
    public void loadSidFile(String sidFileContent) {
        // Parse .sid file format
        // Example format:
        // module: example-module
        // namespace: http://example.com/test
        // sid-range: 10000-10099
        // assignment: container-name 10001
        // assignment: leaf-name 10002
        
        String[] lines = sidFileContent.split("\n");
        String currentNamespace = null;
        long baseSid = 0;
        int size = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            String[] parts = line.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            
            String key = parts[0].trim();
            String value = parts[1].trim();
            
            switch (key) {
                case "namespace":
                    currentNamespace = value;
                    break;
                case "sid-range":
                    String[] range = value.split("-");
                    baseSid = Long.parseLong(range[0].trim());
                    size = Integer.parseInt(range[1].trim()) - (int)baseSid + 1;
                    if (currentNamespace != null) {
                        registerModule(currentNamespace, baseSid, size);
                    }
                    break;
                case "assignment":
                    if (currentNamespace != null) {
                        parseAssignment(value, currentNamespace);
                    }
                    break;
            }
        }
    }
    
    /**
     * Parses a SID assignment line.
     * 
     * @param value the assignment value (node-name sid)
     * @param namespace the namespace
     */
    private void parseAssignment(String value, String namespace) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 2) {
            String nodeName = parts[0];
            long sid = Long.parseLong(parts[1]);
            
            QName qName = new QName(namespace, nodeName);
            qnameToSidMap.put(qName, sid);
            sidToQnameMap.put(sid, qName);
        }
    }
    
    /**
     * Generates a default SID for a YANG node.
     * This is a simplified implementation using hash-based generation.
     * 
     * @param qName the qualified name
     * @return the generated SID
     */
    private Long generateDefaultSid(QName qName) {
        // Use a combination of namespace hash and local name hash
        // Use toString() to ensure consistent string representation
        long nsHash = qName.getNamespace().toString().hashCode() & 0xFFFFFFFFL;
        long nameHash = qName.getLocalName().hashCode() & 0xFFFFFFFFL;
        
        // Combine hashes and ensure it's in valid SID range
        long sid = ((nsHash << 32) | nameHash) & 0x7FFFFFFFFFFFFFFFL;
        
        // Ensure SID is in valid range (positive and reasonable)
        return SID_RANGE_START + (Math.abs(sid) % (SID_RANGE_END - SID_RANGE_START));
    }
    
    /**
     * Generates a node index from the node name.
     * 
     * @param nodeName the node name
     * @return the index
     */
    private int generateNodeIndex(String nodeName) {
        return Math.abs(nodeName.hashCode());
    }
    
    /**
     * Checks if SID-based encoding should be used.
     * 
     * @return true if SID encoding is enabled
     */
    public boolean isSidEncodingEnabled() {
        return !moduleSidRanges.isEmpty() || !qnameToSidMap.isEmpty();
    }
    
    /**
     * SID range information for a module.
     */
    private static class SidRange {
        private final String namespace;
        private final long baseSid;
        private final int size;
        
        public SidRange(String namespace, long baseSid, int size) {
            this.namespace = namespace;
            this.baseSid = baseSid;
            this.size = size;
        }
        
        public String getNamespace() {
            return namespace;
        }
        
        public long getBaseSid() {
            return baseSid;
        }
        
        public int getSize() {
            return size;
        }
    }
}
