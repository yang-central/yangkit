# Quick Reference

## API Quick Reference

### ProtoSchemaGenerator

Generate Protobuf schema from YANG schema.

```java
// Create generator
ProtoSchemaGenerator generator = new ProtoSchemaGenerator();

// Generate file descriptor for module
DescriptorProtos.FileDescriptorProto fileDesc = 
    generator.generateFileDescriptor(yangModule);

// Generate message for data node
DescriptorProtos.DescriptorProto message = 
    generator.generateMessage(dataNode);

// Clear cache
generator.clearCache();
```

### YangProtoTypeMapper

Convert between YANG and Protobuf types and values.

```java
// Get Protobuf type from YANG type
Type yangType = ...;
FieldDescriptorProto.Type protoType = 
    YangProtoTypeMapper.getProtoType(yangType);

// Convert YANG value to Protobuf
Object yangValue = ...;
Object protoValue = 
    YangProtoTypeMapper.convertToProtoValue(yangValue, yangType);

// Convert Protobuf value to YANG
Object protoValue = ...;
Object yangValue = 
    YangProtoTypeMapper.convertToYangValue(protoValue, yangType);
```

### ProtoDescriptorManager

Manage Protobuf descriptors.

```java
// Get manager instance
ProtoDescriptorManager manager = ProtoDescriptorManager.getInstance();

// Get or create descriptor
Descriptors.Descriptor descriptor = 
    manager.getDescriptor(schemaNode);
```

### ProtoCache

High-performance caching service.

```java
// Get cache instance
ProtoCache cache = ProtoCache.getInstance();

// Store value
cache.put("key", value);

// Retrieve value
Object value = cache.get("key");

// Remove value
cache.remove("key");

// Clear all
cache.clear();

// Get statistics
String stats = cache.getStats();

// Get size
int size = cache.size();
```

### ProtoCodecUtil

Unified conversion utilities.

```java
// Convert YANG to Protobuf (with type)
Object protoValue = 
    ProtoCodecUtil.convertYangValueToProto(yangValue, yangType);

// Convert Protobuf to YANG (with type)
Object yangValue = 
    ProtoCodecUtil.convertProtoValueToYang(protoValue, yangType);

// Convert without type info (type inference)
Object protoValue = 
    ProtoCodecUtil.convertYangValueToProto(yangValue, null);
```

## Type Mapping Reference

### Integer Types

| YANG Type | Protobuf Type | Java Type | Range |
|-----------|---------------|-----------|-------|
| int8 | TYPE_INT32 | byte | -128 to 127 |
| int16 | TYPE_INT32 | short | -32,768 to 32,767 |
| int32 | TYPE_INT32 | int | -2³¹ to 2³¹-1 |
| int64 | TYPE_INT64 | long | -2⁶³ to 2⁶³-1 |
| uint8 | TYPE_UINT32 | int | 0 to 255 |
| uint16 | TYPE_UINT32 | int | 0 to 65,535 |
| uint32 | TYPE_UINT32 | long | 0 to 2³²-1 |
| uint64 | TYPE_UINT64 | long | 0 to 2⁶⁴-1 |

### Other Primitive Types

| YANG Type | Protobuf Type | Java Type | Notes |
|-----------|---------------|-----------|-------|
| boolean | TYPE_BOOL | boolean | true/false |
| string | TYPE_STRING | String | UTF-8 encoded |
| decimal64 | TYPE_DOUBLE | double | IEEE 754 |
| binary | TYPE_BYTES | ByteString | Base64 encoded |
| empty | TYPE_BOOL | boolean | present=true, absent=false |

### Enumeration

| YANG Type | Protobuf Type | Java Type | Notes |
|-----------|---------------|-----------|-------|
| enumeration | TYPE_ENUM | int/str | Value→Int, Name→String |

### Time Types

| YANG Type | Protobuf Type | Java Type | Representation |
|-----------|---------------|-----------|----------------|
| date-and-time | TYPE_INT64 | long | Milliseconds since epoch (UTC) |
| date-only | TYPE_INT32 | int | Days since epoch |
| time-of-day | TYPE_INT64 | long | Milliseconds since midnight |

## Value Conversion Examples

### Integers

```java
// With type information
YangInteger intType = ...; // int32 type
Integer val = 42;
Object proto = YangProtoTypeMapper.convertToProtoValue(val, intType);
// Result: 42 (Integer)

// Without type information
Integer val = 42;
Object proto = YangProtoTypeMapper.convertToProtoValue(val, null);
// Result: 42 (Integer preserved)
```

### Booleans

```java
Boolean val = true;
Object proto = YangProtoTypeMapper.convertToProtoValue(val, null);
// Result: true (Boolean preserved)
```

### Strings

```java
String val = "hello";
Object proto = YangProtoTypeMapper.convertToProtoValue(val, null);
// Result: "hello" (String preserved)
```

### Numbers

```java
Long val = 1234567890L;
Object proto = YangProtoTypeMapper.convertToProtoValue(val, null);
// Result: 1234567890L (Long preserved)

Double val = 3.14159;
Object proto = YangProtoTypeMapper.convertToProtoValue(val, null);
// Result: 3.14159 (Double preserved)
```

### Binary Data

```java
// To Protobuf
byte[] data = {...};
Object proto = YangProtoTypeMapper.convertToProtoValue(data, binaryType);
// Result: ByteString

// From Protobuf
ByteString protoBytes = ...;
Object data = YangProtoTypeMapper.convertToYangValue(protoBytes, binaryType);
// Result: byte[]
```

### Decimal64

```java
BigDecimal val = new BigDecimal("123.456");
Object proto = YangProtoTypeMapper.convertToProtoValue(val, decimalType);
// Result: 123.456 (Double)
```

## Cache Operations

### Basic Usage

```java
ProtoCache cache = ProtoCache.getInstance();

// Put and get
cache.put("my-key", myDescriptor);
Descriptors.Descriptor desc = cache.get("my-key");

// Check if exists (try to get)
Descriptors.Descriptor desc = cache.get("non-existent");
if (desc == null) {
    // Not in cache, need to create
}
```

### Cache Statistics

```java
String stats = cache.getStats();
// Example output: "Cache stats: size=5, totalAccesses=100, ..."

int size = cache.size();
// Returns current number of entries
```

### Cache Cleanup

```java
// Remove specific entry
cache.remove("old-key");

// Clear all entries
cache.clear();

// Automatic expiration (configured by TTL)
// Default: 30 minutes
```

## Common Patterns

### Pattern 1: Type-Safe Conversion

```java
// When you have type information
Type yangType = leaf.getType();
Object yangValue = leaf.getValue();

// Convert with full type safety
Object protoValue = YangProtoTypeMapper.convertToProtoValue(
    yangValue, yangType);
```

### Pattern 2: Type Inference

```java
// When type information is unavailable
Object yangValue = ...; // Could be any type

// Type will be inferred from the value itself
Object protoValue = YangProtoTypeMapper.convertToProtoValue(
    yangValue, null);
    
// Boolean stays Boolean
// Number stays Number  
// Other types become String
```

### Pattern 3: Using Cache

```java
// Always check cache first
String key = generateKey(schemaNode);
Descriptors.Descriptor cached = cache.get(key);

if (cached != null) {
    // Use cached descriptor
    return cached;
}

// Create new descriptor
Descriptors.Descriptor descriptor = createDescriptor(schemaNode);

// Store in cache for future use
cache.put(key, descriptor);

return descriptor;
```

### Pattern 4: Batch Processing

```java
// Process multiple related nodes efficiently
ProtoDescriptorManager manager = ProtoDescriptorManager.getInstance();

// Get parent descriptor first (will be cached)
Descriptors.Descriptor parentDesc = manager.getDescriptor(parentNode);

// Process children (can reuse parent's cached info)
for (DataNode child : parent.getDataChildren()) {
    Descriptors.Descriptor childDesc = manager.getDescriptor(child);
    // ... process child
}
```

## Error Handling

### Null Safety

```java
// All methods handle null gracefully
Object result = YangProtoTypeMapper.convertToProtoValue(null, type);
// Returns: null

Object result = YangProtoTypeMapper.convertToProtoValue(value, null);
// Returns: value.toString() (or preserves type if Boolean/Number)

Descriptors.Descriptor desc = manager.getDescriptor(null);
// Returns: null
```

### Exception Handling

```java
try {
    DescriptorProtos.FileDescriptorProto fileDesc = 
        generator.generateFileDescriptor(module);
} catch (Exception e) {
    // Handle generation error
    logger.error("Failed to generate descriptor", e);
}
```

## Performance Tips

### 1. Reuse Instances

```java
// GOOD: Reuse generator
ProtoSchemaGenerator generator = new ProtoSchemaGenerator();
for (Module module : modules) {
    DescriptorProtos.FileDescriptorProto fd = 
        generator.generateFileDescriptor(module);
}

// BAD: Create new generator each time
for (Module module : modules) {
    ProtoSchemaGenerator gen = new ProtoSchemaGenerator();
    DescriptorProtos.FileDescriptorProto fd = 
        gen.generateFileDescriptor(module);
}
```

### 2. Leverage Caching

```java
// Access frequently used descriptors
// They will be automatically cached
for (int i = 0; i < 1000; i++) {
    Descriptors.Descriptor desc = manager.getDescriptor(schemaNode);
    // Second access will hit cache
}
```

### 3. Monitor Cache

```java
// Periodically check cache performance
ProtoCache cache = ProtoCache.getInstance();
System.out.println(cache.getStats());

// Clear if memory pressure
if (cache.size() > threshold) {
    cache.clear();
}
```

## Testing Examples

### Unit Test Template

```java
@Test
public void testMyConversion() {
    // Arrange
    Object inputValue = ...;
    Type expectedType = ...;
    
    // Act
    Object result = YangProtoTypeMapper.convertToProtoValue(
        inputValue, expectedType);
    
    // Assert
    Assert.assertNotNull(result);
    Assert.assertEquals(expectedClass, result.getClass());
}
```

### Cache Test Template

```java
@Test
public void testCacheOperations() {
    ProtoCache cache = ProtoCache.getInstance();
    cache.clear();
    
    // Test put/get
    cache.put("key", "value");
    Assert.assertEquals("value", cache.get("key"));
    
    // Test remove
    cache.remove("key");
    Assert.assertNull(cache.get("key"));
    
    // Clean up
    cache.clear();
}
```

## Troubleshooting

### Issue: Type Conversion Returns String

**Problem:** Values converting to String when expecting original type

**Solution:** Ensure type information is provided or value is Boolean/Number

```java
// Without type - may convert to String
Object result = convert(value, null);

// With type - proper conversion
Object result = convert(value, yangType);
```

### Issue: Cache Miss Rate High

**Problem:** Low cache hit rate affecting performance

**Solutions:**
1. Increase cache TTL: Modify `expireTimeMs` in ProtoCache
2. Increase cache size: Modify `maxSize` in ProtoCache
3. Review access patterns - ensure related operations are batched

### Issue: Memory Growth

**Problem:** Memory usage growing over time

**Solutions:**
1. Call `cache.clear()` periodically
2. Reduce `maxSize` configuration
3. Reduce `expireTimeMs` configuration
4. Profile for descriptor leaks

## Maven Commands

### Build

```bash
mvn clean package
```

### Run Tests

```bash
mvn test
```

### Check Dependencies

```bash
mvn dependency:tree
```

### Generate Javadoc

```bash
mvn javadoc:javadoc
```
