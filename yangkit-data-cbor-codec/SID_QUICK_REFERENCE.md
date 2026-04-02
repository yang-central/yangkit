# SID Quick Reference

## Core API

### SidManager - SID Management

```java
public class SidQuickReferenceManagerExample {
    public void run() {
        // Create manager
        SidManager sidManager = new SidManager();

        // Register module SID range
        sidManager.registerModule("http://example.com/module", 10000, 100);

        // Get SID
        QName qName = new QName("http://example.com/module", "node-name");
        Long sid = sidManager.getSid(qName);

        // Reverse lookup
        QName resolved = sidManager.getQName(sid);

        // Load .sid file
        String content = "namespace: http://example.com/module\n"
                + "sid-range: 10000-10099\n"
                + "assignment: node-name 10001\n";
        sidManager.loadSidFile(content);
    }
}
```

### SidEncoder - Encoding Utility

```java
// Encode to JSON using SIDs
ObjectNode json = SidEncoder.encodeWithSid(container, sidManager);

// Decode back to original names
ObjectNode original = SidEncoder.decodeWithSid(json, sidManager);
```

### SidContainerDataCborCodec - CBOR Codec

```java
// Create codec
SidContainerDataCborCodec codec = 
    new SidContainerDataCborCodec(schemaNode, sidManager);

// Serialize to CBOR using the current SID-oriented flow
byte[] cbor = codec.serialize(containerData);

// Deserialize from CBOR
ContainerData data = codec.deserialize(cbor, validator);
```

### SidCborEncoder - High-level API

```java
// Encode to CBOR (current helper flow adds the default SID tag)
byte[] cbor = SidCborEncoder.encodeToCbor(container, sidManager);

// Decode from CBOR (current helper flow handles the default SID tag path)
JsonNode result = SidCborEncoder.decodeFromCbor(cbor, sidManager);
```

## .sid File Format

```
# Comment
namespace: http://example.com/module
sid-range: 10000-10099
assignment: node-name 10001
assignment: another-node 10002
```

## CBOR Tag

- **Range**: 60000-60999 (RFC 9254 SID tag range)
- **Default helper path**: 60000
- **Purpose**: Identifies SID-encoded YANG data in the current helper flow

## Encoding Comparison

### Name-based (Section 4)
```json
{
  "interface": {
    "name": "eth0",
    "mtu": 1500
  }
}
```

### SID-based (Section 5)
```json
{
  "10001": {
    "10002": "eth0",
    "10003": 1500
  }
}
```

## Complete Example

```java
import org.yangcentral.yangkit.data.codec.cbor.*;

public class Example {
    static void main(String[] args) throws Exception {
        // 1. Configure SID manager
        SidManager sm = new SidManager();
        sm.registerModule("http://example.com/test", 10000, 100);
        
        // 2. Prepare data (requires actual schema nodes)
        // Container schema = ...;
        // ContainerData data = new ContainerDataImpl(schema);
        // ... populate data
        
        // 3. Encode using SID
        SidContainerDataCborCodec codec = 
            new SidContainerDataCborCodec(schema, sm);
        byte[] cbor = codec.serialize(data);
        
        // 4. Decode using SID
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        ContainerData decoded = codec.deserialize(cbor, validator);
    }
}
```

## Method Quick Reference

| Class | Method | Description |
|-------|--------|-------------|
| SidManager | registerModule(ns, base, size) | Register module SID range |
| SidManager | getSid(qName) | Get SID for a node |
| SidManager | getQName(sid) | SID → QName |
| SidManager | loadSidFile(content) | Load .sid file |
| SidEncoder | encodeWithSid(container, sm) | SID encoding |
| SidEncoder | decodeWithSid(json, sm) | SID decoding |
| SidCborEncoder | encodeToCbor(container, sm) | Encode to CBOR |
| SidCborEncoder | decodeFromCbor(bytes, sm) | Decode from CBOR |

## Reference Documentation

- 📖 [SID_SUPPORT.md](./SID_SUPPORT.md) - Detailed usage documentation
- 📖 [SID_IMPLEMENTATION_SUMMARY.md](./SID_IMPLEMENTATION_SUMMARY.md) - Implementation summary
- 🔗 RFC 9254 - https://www.rfc-editor.org/rfc/rfc9254
- 🔗 RFC 9176 - https://www.rfc-editor.org/rfc/rfc9176
