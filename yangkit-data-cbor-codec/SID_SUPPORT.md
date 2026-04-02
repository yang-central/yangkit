# SID-based CBOR Encoding Support

## Overview

This document describes the module's current **SID (Schema Item Identifier)** based encoding support for the mechanisms described in **RFC 9254 Section 5**.

SID is a unique 64-bit integer identifier assigned to YANG schema nodes, which can be used instead of string node names to reduce CBOR encoding size. The current implementation focuses on the tested core SID-based encoding/decoding paths and documents remaining gaps explicitly below.

## Key Components

### 1. SidManager

Manages the mapping between YANG schema nodes (QName) and their SIDs.

**Features:**
- Register YANG modules with SID ranges
- Generate SIDs for schema nodes
- Cache QName ↔ SID mappings
- Load SID assignments from `.sid` files
- Reverse lookup (SID → QName)

**Example:**
```java
SidManager sidManager = new SidManager();

// Register a module with its SID range
sidManager.registerModule("http://example.com/test", 10000, 100);

// Get SID for a schema node
QName qName = new QName("http://example.com/test", "container-name");
Long sid = sidManager.getSid(qName);  // Returns SID for this node
```

### 2. SidEncoder

Provides utility methods for SID-based encoding and decoding.

**Methods:**
- `encodeWithSid(ContainerData container, SidManager sidManager)` - Encode container using SIDs
- `decodeWithSid(JsonNode jsonNode, SidManager sidManager)` - Decode SID-encoded JSON

**Example:**
```java
// Encode container data using SIDs
ObjectNode sidEncoded = SidEncoder.encodeWithSid(containerData, sidManager);

// Decode back to original names
ObjectNode decoded = SidEncoder.decodeWithSid(sidEncoded, sidManager);
```

### 3. SidContainerDataCborCodec

A variant of `ContainerDataCborCodec` that uses the module's current SID-based encoding path.

**Usage:**
```java
Container schemaNode = ...; // Get container schema
SidManager sidManager = ...; // Create and configure SID manager

// Create SID-aware codec
SidContainerDataCborCodec codec = new SidContainerDataCborCodec(schemaNode, sidManager);

// Serialize to CBOR with SIDs
byte[] cborBytes = codec.serialize(containerData);

// Deserialize from CBOR
ContainerData data = codec.deserialize(cborBytes, validatorResultBuilder);
```

### 4. SidCborEncoder

High-level API for the current SID-based CBOR encoding flow with CBOR tags.

**Methods:**
- `encodeToCbor(ContainerData container, SidManager sidManager)` - Encode to tagged CBOR
- `decodeFromCbor(byte[] cborBytes, SidManager sidManager)` - Decode from tagged CBOR

**Example:**
```java
// Encode with CBOR tag (60000-60999 range)
byte[] cborBytes = SidCborEncoder.encodeToCbor(containerData, sidManager);

// Decode from CBOR with automatic tag handling
JsonNode result = SidCborEncoder.decodeFromCbor(cborBytes, sidManager);
```

## RFC 9254 Alignment (Current Implementation)

### CBOR Tag Range
- **Tag 60000-60999**: RFC 9254 defines this range for SID-based encoded YANG data
- The current helper flow uses tag **60000** in its default path

### Encoding Format

**Name-based encoding (Section 4):**
```json
{
  "interface": {
    "name": "eth0",
    "mtu": 1500
  }
}
```

**SID-based encoding (Section 5):**
```json
{
  "10001": {  // SID instead of "interface"
    "10002": "eth0",   // SID instead of "name"
    "10003": 1500      // SID instead of "mtu"
  }
}
```

## SID Assignment

### Automatic Generation

By default, SIDs are generated based on:
1. Module namespace hash
2. Node local name hash
3. Configured SID ranges

### Manual Assignment (.sid files)

You can load explicit SID assignments from `.sid` files:

```
# Example .sid file format
namespace: http://example.com/test
sid-range: 10000-10099
assignment: interface 10001
assignment: name 10002
assignment: mtu 10003
```

Load it with:
```java
String sidFileContent = Files.readString(Paths.get("module.sid"));
sidManager.loadSidFile(sidFileContent);
```

## Benefits of SID-based Encoding

1. **Reduced Size**: Integer SIDs are often more compact than string node names
2. **Simpler Field Identifiers**: Numeric identifiers can reduce payload verbosity in the current SID flow
3. **Standards-aligned scope**: Targets the SID-based mechanisms described in RFC 9254 Section 5 within the currently implemented paths
4. **Can coexist with name-based encoding**: The module keeps the name-based path available alongside SID-oriented helpers

## When to Use SID

✅ **Use SID-based encoding when:**
- Bandwidth or storage is constrained
- You have control over both encoder and decoder
- You need compact SID-based CBOR encoding within the module's currently tested scope
- Performance is critical

❌ **Use name-based encoding when:**
- Human readability is important
- Interoperability with non-SID-aware systems
- Schema evolution is frequent (SIDs are stable identifiers)

## Complete Example

```java
import org.yangcentral.yangkit.data.codec.cbor.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;

public class SidExample {
    
    public static void main(String[] args) throws Exception {
        // 1. Create and configure SID manager
        SidManager sidManager = new SidManager();
        sidManager.registerModule("http://example.com/test", 10000, 100);
        
        // 2. Prepare sample data
        Container schemaNode = ...; // Get container schema from parsed YANG model
        ContainerData containerData = new ContainerDataImpl(schemaNode);
        // ... populate container data
        
        // 3. Encode using SID
        SidContainerDataCborCodec codec = new SidContainerDataCborCodec(schemaNode, sidManager);
        byte[] cborBytes = codec.serialize(containerData);
        
        // 4. Decode back
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        ContainerData decoded = codec.deserialize(cborBytes, validatorBuilder);
        
        System.out.println("Successfully encoded/decoded with SID!");
    }
}
```

## API Reference

### SidManager Methods

| Method | Description |
|--------|-------------|
| `registerModule(namespace, baseSid, size)` | Register a module with SID range |
| `getSid(qName)` | Get SID for a schema node |
| `getQName(sid)` | Get QName from SID |
| `loadSidFile(content)` | Load SID assignments from .sid file |
| `isSidEncodingEnabled()` | Check if SID encoding is active |

### SidEncoder Methods

| Method | Description |
|--------|-------------|
| `encodeWithSid(container, sidManager)` | Encode container with SIDs |
| `decodeWithSid(jsonNode, sidManager)` | Decode SID-encoded JSON |

### SidCborEncoder Methods

| Method | Description |
|--------|-------------|
| `encodeToCbor(container, sidManager)` | Encode to CBOR with SID tag |
| `decodeFromCbor(cborBytes, sidManager)` | Decode from tagged CBOR |

## Implementation Status

✅ **Currently implemented:**
- SidManager with caching and module registration
- SID-based encoding for Leaf, LeafList, Container
- SID-based decoding with field name resolution
- CBOR tag handling in the current helper/codec flow (default path uses tag 60000)
- .sid file loading
- Recursive nested container encoding

⚠️ **Simplified (for future enhancement):**
- LeafList deserialization (requires value construction)
- List entry encoding/decoding
- Full schema node resolution during decode
- AnyXML/AnyData support

## Testing

Run tests with:
```bash
mvn test
```

Test classes:
- `SidManagerTest` - Unit tests for SID management
- `SidCborIntegrationTest` - Integration tests for the current encode/decode cycle

## References

- **RFC 9254**: YANG Data Model in Concise Binary Object Representation (CBOR)
  - Section 4: Name-based encoding
  - Section 5: SID-based encoding
- **RFC 9176**: YANG Schema Item iDentifier (SID)
