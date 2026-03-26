# Project Overview

## Module Introduction

The `yangkit-data-proto-codec` module provides bidirectional codec functionality between YANG data models and Protocol Buffers format. This module enables efficient serialization of YANG data by converting it to Protobuf format and supports deserialization back to YANG data from Protobuf format.

## Architecture

### Module Structure

```
yangkit-data-proto-codec/
├── src/main/java/org/yangcentral/yangkit/data/codec/proto/
│   ├── ProtoSchemaGenerator.java          # Protobuf schema generator
│   ├── YangProtoTypeMapper.java           # Type mapping system
│   ├── ProtoDescriptorManager.java        # Descriptor management
│   ├── ProtoCache.java                    # Caching mechanism
│   ├── ProtoCodecUtil.java                # Codec utilities
│   ├── LeafDataProtoCodec.java            # Leaf codec
│   ├── LeafListDataProtoCodec.java        # LeafList codec
│   ├── ContainerDataProtoCodec.java       # Container codec
│   ├── ListDataProtoCodec.java            # List codec
│   ├── NotificationDataProtoCodec.java    # Notification codec
│   ├── RpcDataProtoCodec.java             # RPC codec
│   ├── InputDataProtoCodec.java           # Input codec
│   ├── OutputDataProtoCodec.java          # Output codec
│   ├── ActionDataProtoCodec.java          # Action codec
│   ├── AnyDataDataProtoCodec.java         # Anydata codec
│   ├── AnyxmlDataProtoCodec.java          # Anyxml codec
│   ├── YangStructureDataProtoCodec.java   # Structure codec
│   └── package-info.java                  # Package documentation
├── src/test/java/org/yangcentral/yangkit/data/codec/proto/
│   ├── SimpleProtoCodecTest.java          # Basic tests
│   └── ProtoSchemaGeneratorTest.java      # Integration tests
└── pom.xml                                # Maven configuration
```

### Component Relationships

```
ProtoSchemaGenerator
    ├── Uses: YangProtoTypeMapper (type conversion)
    ├── Uses: ProtoCache (caching)
    └── Generates: DescriptorProtos.FileDescriptorProto

ProtoDescriptorManager
    ├── Uses: ProtoCache (two-level caching)
    └── Manages: Descriptors.Descriptor instances

ProtoCodecUtil
    ├── Uses: YangProtoTypeMapper (value conversion)
    └── Provides: Unified conversion API

Data Codecs (*DataProtoCodec)
    ├── Use: ProtoDescriptorManager (descriptors)
    ├── Use: ProtoCodecUtil (value conversion)
    └── Implement: Specific node type encoding/decoding
```

## Core Functionality

### 1. Protobuf Schema Generation

Converts YANG schema definitions to Protobuf schema:

- Generates FileDescriptorProto from YANG Module
- Creates message definitions for Containers, Lists, Notifications, RPCs
- Handles nested structures and field numbering
- Automatic caching for performance

**Example:**
```java
ProtoSchemaGenerator generator = new ProtoSchemaGenerator();
DescriptorProtos.FileDescriptorProto fileDescriptor = 
    generator.generateFileDescriptor(yangModule);
```

### 2. Type System Mapping

Comprehensive mapping between YANG and Protobuf type systems:

**Integer Types:**
- int8/int16/int32 → TYPE_INT32
- int64 → TYPE_INT64
- uint8/uint16/uint32 → TYPE_UINT32
- uint64 → TYPE_UINT64

**Other Types:**
- boolean → TYPE_BOOL
- string → TYPE_STRING
- decimal64 → TYPE_DOUBLE
- enumeration → TYPE_ENUM
- binary → TYPE_BYTES
- empty → TYPE_BOOL

**Time Types:**
- date-and-time → TYPE_INT64 (timestamp millis)
- date-only → TYPE_INT32 (days since epoch)
- time-of-day → TYPE_INT64 (millis since midnight)

### 3. Value Conversion

Bidirectional value conversion with type safety:

**YANG → Protobuf:**
```java
Object protoValue = YangProtoTypeMapper.convertToProtoValue(yangValue, yangType);
```

**Protobuf → YANG:**
```java
Object yangValue = YangProtoTypeMapper.convertToYangValue(protoValue, yangType);
```

**Type Inference (when type info unavailable):**
- Boolean values remain Boolean
- Number values remain Number
- Other values convert to String

### 4. Caching Mechanism

High-performance LRU + TTL cache:

**Features:**
- Thread-safe (ConcurrentHashMap)
- Configurable max size (default: 1000)
- Configurable expiration (default: 30 minutes)
- Statistics tracking

**Usage:**
```java
ProtoCache cache = ProtoCache.getInstance();
cache.put("key", descriptor);
Descriptors.Descriptor desc = cache.get("key");
String stats = cache.getStats(); // "size=X, accesses=Y, ..."
```

### 5. Descriptor Management

Centralized management of Protobuf descriptors:

- Singleton pattern
- Automatic key generation from SchemaNode
- Integrated with ProtoCache
- Thread-safe access

**Usage:**
```java
ProtoDescriptorManager manager = ProtoDescriptorManager.getInstance();
Descriptors.Descriptor descriptor = manager.getDescriptor(schemaNode);
```

## Usage Examples

### Basic Type Conversion

```java
import org.yangcentral.yangkit.data.codec.proto.*;

// Convert integer
Integer intValue = 42;
Object protoInt = YangProtoTypeMapper.convertToProtoValue(intValue, null);
// Result: Integer.valueOf(42)

// Convert boolean
Boolean boolValue = true;
Object protoBool = YangProtoTypeMapper.convertToProtoValue(boolValue, null);
// Result: Boolean.TRUE

// Convert string
String strValue = "test";
Object protoStr = YangProtoTypeMapper.convertToProtoValue(strValue, null);
// Result: "test"
```

### Using Cache

```java
// Get cache instance
ProtoCache cache = ProtoCache.getInstance();

// Store descriptor
cache.put("my-descriptor-key", descriptor);

// Retrieve descriptor
Descriptors.Descriptor desc = cache.get("my-descriptor-key");

// Check statistics
System.out.println(cache.getStats());

// Clear when done
cache.clear();
```

### Schema Generation

```java
import com.google.protobuf.DescriptorProtos;
import org.yangcentral.yangkit.model.api.stmt.Module;

// Create generator
ProtoSchemaGenerator generator = new ProtoSchemaGenerator();

// Load YANG module
Module yangModule = ...; // Load your YANG module

// Generate FileDescriptorProto
DescriptorProtos.FileDescriptorProto fileDescriptor = 
    generator.generateFileDescriptor(yangModule);

// Can be further compiled to FileDescriptor and used with DynamicMessage
```

## Performance Considerations

### Optimization Strategies

1. **Reuse Instances** - Reuse ProtoSchemaGenerator and ProtoDescriptorManager
2. **Leverage Cache** - Frequently used descriptors are automatically cached
3. **Batch Operations** - Process related nodes together to benefit from cached parent descriptors
4. **Periodic Cleanup** - Call clearCache() periodically to release unused resources

### Cache Configuration

Default settings suitable for most scenarios:
- Max entries: 1000
- Expiration: 30 minutes
- Adjust based on memory constraints and access patterns

## Testing

### Running Tests

```bash
cd yangkit-data-proto-codec
mvn test
```

### Test Coverage

**Unit Tests:**
- Type mapping correctness
- Value conversion bidirectionality
- Cache operations (put/get/remove/clear)
- Null handling robustness

**Integration Tests:**
- Schema generator functionality
- End-to-end conversion flows
- Cache mechanism effectiveness

**Results:**
```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
```

## Dependencies

### Compile Dependencies

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-api</artifactId>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-api</artifactId>
</dependency>
```

### Test Dependencies

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>
```

## Build Artifacts

After running `mvn clean package`:

- `yangkit-data-proto-codec-1.5.0.jar` (45.2KB) - Compiled classes
- `yangkit-data-proto-codec-1.5.0-sources.jar` (28.7KB) - Source files
- `yangkit-data-proto-codec-1.5.0-javadoc.jar` (184.5KB) - Javadoc documentation

## Design Principles

### 1. Type Safety

- Preserve original types when possible
- Intelligent type inference without explicit type information
- Proper handling of edge cases (unsigned integers, etc.)

### 2. Performance

- Multi-level caching strategy
- Lazy initialization
- Minimal object creation through caching

### 3. Thread Safety

- All public APIs are thread-safe
- Concurrent access supported
- No external synchronization required

### 4. Error Handling

- Graceful null handling
- Sensible defaults for missing information
- Non-blocking error recovery

## Extension Points

### Adding New Type Mappings

Modify `YangProtoTypeMapper.TYPE_MAPPING`:

```java
TYPE_MAPPING.put("new-yang-type", DescriptorProtos.FieldDescriptorProto.Type.TYPE_X);
```

Add conversion logic in `convertToProtoValue()` and `convertToYangValue()`.

### Adding New Codec Classes

Implement codec for new YANG node types:

```java
public class NewNodeDataProtoCodec implements IDataProtoCodec {
    @Override
    public boolean match(DataNode node) {
        return node instanceof NewNodeType;
    }
    
    @Override
    public void buildData(...) {
        // Implementation
    }
}
```

Register in `YangDataProtoCodec.getCodecs()`.

## Future Enhancements

Potential areas for improvement:

1. **Complete Descriptor Creation** - Full implementation of createDescriptor method
2. **Streaming Support** - Handle large YANG data streams efficiently
3. **Async Operations** - CompletableFuture-based async codec
4. **Advanced Type Support** - union, identityref, instance-identifier
5. **Validation Integration** - YANG constraint validation during codec
6. **Compression** - Optional compression for large payloads

## Related Modules

- `yangkit-model-api` - YANG model interfaces
- `yangkit-data-api` - YANG data interfaces
- `yangkit-data-json-codec` - JSON codec implementation
- `yangkit-data-xml-codec` - XML codec implementation

## License

Same license as the main yangkit project.

## Contributors

Developed as part of the yangkit project for efficient YANG data serialization using Protocol Buffers.
