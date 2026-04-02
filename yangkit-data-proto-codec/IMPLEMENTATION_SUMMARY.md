# Implementation Summary

## Overview

The `yangkit-data-proto-codec` module provides the current bidirectional codec functionality between YANG data models and the generated Protocol Buffers structures used by this project. This document summarizes the present implementation details and tested areas.

## Core Components Implemented

### 1. ProtoSchemaGenerator

**Location:** `src/main/java/org/yangcentral/yangkit/data/codec/proto/ProtoSchemaGenerator.java`

**Functionality:**
- Generates Protobuf FileDescriptorProto from YANG modules
- Creates message definitions for different YANG data structures (Container, List, Notification, RPC, etc.)
- Handles nested messages and field numbering automatically
- Built-in caching mechanism for performance optimization

**Key Methods:**
- `generateFileDescriptor(Module module)` - Generates file descriptor for a YANG module
- `generateMessage(SchemaNode dataNode)` - Generates a Protobuf message from a supported YANG schema node, including `YangStructure`
- `addContainerFields()` - Adds fields for container nodes
- `addListFields()` - Adds fields for list nodes with key support
- `addLeafField()` - Adds leaf field to message
- `addLeafListField()` - Adds repeated leaf-list field

### 2. YangProtoTypeMapper

**Location:** `src/main/java/org/yangcentral/yangkit/data/codec/proto/YangProtoTypeMapper.java`

**Type Mapping Table:**

| YANG Type | `SIMPLE` mode | `YGOT` mode | Description |
|-----------|---------------|-------------|-------------|
| int8, int16, int32 | TYPE_INT32 | TYPE_MESSAGE (`.ywrapper.IntValue`) | Signed integer |
| int64 | TYPE_INT64 | TYPE_MESSAGE (`.ywrapper.IntValue`) | Long integer |
| uint8, uint16, uint32 | TYPE_UINT32 | TYPE_MESSAGE (`.ywrapper.UintValue`) | Unsigned integer |
| uint64 | TYPE_UINT64 | TYPE_MESSAGE (`.ywrapper.UintValue`) | Unsigned long |
| boolean | TYPE_BOOL | TYPE_MESSAGE (`.ywrapper.BoolValue`) | Boolean value |
| string | TYPE_STRING | TYPE_MESSAGE (`.ywrapper.StringValue`) | String |
| decimal64 | TYPE_STRING | TYPE_MESSAGE (`.ywrapper.Decimal64Value`) | Preserves lexical value in `SIMPLE`; `YGOT` wrapper carries `digits` + `precision` |
| enumeration / bits | TYPE_ENUM | TYPE_ENUM | Generated enum descriptor |
| binary | TYPE_BYTES | TYPE_MESSAGE (`.ywrapper.BytesValue`) | Binary data |
| empty | TYPE_BOOL | TYPE_MESSAGE (`.ywrapper.BoolValue`) | Presence/absence scalar |
| identityref | TYPE_STRING | TYPE_MESSAGE (`.ywrapper.StringValue`) | QName string form |

Types that are not recognized by the built-in mapper currently fall back to string handling.

**Conversion Methods:**
- `getProtoType(Type yangType)` - Convenience mapping using `SIMPLE` mode
- `getProtoFieldType(Type yangType, ProtoCodecMode mode)` - Mode-aware schema mapping for SIMPLE vs YGOT
- `convertToProtoValue(Object value, Type yangType)` - Converts YANG value to Protobuf format
- `convertToYangValue(Object value, Type yangType)` - Converts Protobuf value back to YANG format

### 3. ProtoDescriptorManager

**Location:** `src/main/java/org/yangcentral/yangkit/data/codec/proto/ProtoDescriptorManager.java`

**Features:**
- Singleton pattern ensuring global unique instance
- Automatic descriptor key generation based on SchemaNode
- Two-level caching integration with ProtoCache
- Thread-safe concurrent access

### 4. ProtoCache

**Location:** `src/main/java/org/yangcentral/yangkit/data/codec/proto/ProtoCache.java`

**Cache Strategy:**
- LRU (Least Recently Used) eviction policy
- Time-based expiration (default 30 minutes)
- Maximum capacity limit (default 1000 entries)
- Thread-safe concurrent access using ConcurrentHashMap
- Statistics tracking (access count, hit rate, etc.)

**API:**
- `getInstance()` - Get singleton cache instance
- `put(String key, T value)` - Store value in cache
- `get(String key)` - Retrieve value from cache
- `remove(String key)` - Remove entry from cache
- `clear()` - Clear all cache entries
- `getStats()` - Get cache statistics

### 5. ProtoCodecUtil

**Location:** `src/main/java/org/yangcentral/yangkit/data/codec/proto/ProtoCodecUtil.java`

**Utility Methods:**
- `convertYangValueToProto(Object value, Object type)` - Unified YANG to Protobuf conversion
- `convertProtoValueToYang(Object value, Object type)` - Unified Protobuf to YANG conversion
- Type inference when type information is not available

### 6. Data Codecs

Main-code codec classes currently present for different YANG data types:

- **LeafDataProtoCodec** - Leaf node encoding/decoding
- **LeafListDataProtoCodec** - Leaf-list encoding/decoding  
- **ContainerDataProtoCodec** - Container node encoding/decoding
- **ListDataProtoCodec** - List node encoding/decoding
- **NotificationDataProtoCodec** - Notification encoding/decoding
- **RpcDataProtoCodec** - RPC operation encoding/decoding
- **InputDataProtoCodec** - RPC input encoding/decoding
- **OutputDataProtoCodec** - RPC output encoding/decoding
- **ActionDataProtoCodec** - Action operation encoding/decoding
- **AnyDataDataProtoCodec** - Anydata node encoding/decoding via a generated wrapper message whose `value` field carries JSON text
- **AnyxmlDataProtoCodec** - Anyxml node encoding/decoding
- **YangStructureDataProtoCodec** - YANG structure encoding/decoding

These classes do not all have the same maturity level or interoperability guarantees; current regression evidence is strongest for value conversion, `anydata` wrapper handling, keyed `List` round-trips, repeated child collection handling, RPC input/output descriptor and round-trip paths, and `YangStructure` descriptor plus SIMPLE/YGOT round-trip paths.

Current highlighted coverage areas include:

- `ListDataProtoCodec`: SIMPLE/YGOT descriptor generation and keyed-entry round-trip coverage
- repeated child collection handling inside `repeated-container`: SIMPLE/YGOT repeated `leaf-list` ordering and repeated keyed `list` entry round-trips
- `RpcDataProtoCodec` / `InputDataProtoCodec` / `OutputDataProtoCodec`: RPC descriptor coverage plus executable simple and nested RPC regression cases in SIMPLE/YGOT mode
- `YangStructureDataProtoCodec`: SIMPLE/YGOT descriptor generation and structure-content round-trip coverage

## Anydata Handling

This section collects the implementation-specific `anydata` behavior of the proto codec.
The module README covers user-facing usage, while this document keeps the lower-level representation details together.

### Current Representation

For `anydata` nodes, the current protobuf codec generates a wrapper protobuf message for the `anydata` schema node.
Its payload is carried in a `value` field as JSON text, and deserialization resolves the payload schema through `AnydataValidationOptions`.

This design reuses the existing JSON document parsing logic used by the other codecs; it does not imply protobuf-level wire compatibility with other `anydata` representations.

Typical behavior:

- outer protobuf message identifies the `anydata` schema node
- embedded JSON payload is read from the generated wrapper message
- payload schema is resolved by rule, schema-node registration, or default context
- if no context matches, the `anydata` node still exists but its payload may have zero recognized children

### Validation Options

- `AnydataValidationOptions` matching order is: rule > schema-node registration > default context
- rule-based matching can use `request.getSourcePath()` when the same schema node may carry different payload schema sets in different locations
- the shared API itself is documented in [`../yangkit-data-api/README.md`](../yangkit-data-api/README.md)

### Complete Minimal Runnable Example

```java
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.proto.ProtoDescriptorManager;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangYinParser;

public class ProtoAnydataExample {
    public static void main(String[] args) throws Exception {
        YangSchemaContext outerSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/outer/yang");
        YangSchemaContext payloadSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/payload/yang");

        if (!outerSchemaContext.validate().isOk() || !payloadSchemaContext.validate().isOk()) {
            throw new IllegalStateException("schema validation failed");
        }

        Container wrapperContainer = null;
        for (Module module : outerSchemaContext.getModules()) {
            if ("outer-anydata".equals(module.getArgStr())) {
                wrapperContainer = (Container) module.getDataNodeChildren().get(0);
                break;
            }
        }
        if (wrapperContainer == null) {
            throw new IllegalStateException("wrapper container not found");
        }

        Descriptors.Descriptor wrapperDescriptor = ProtoDescriptorManager.getInstance().getDescriptor(wrapperContainer);
        Descriptors.FieldDescriptor payloadHolderField = wrapperDescriptor.findFieldByName("payload_holder");
        Descriptors.FieldDescriptor valueField = payloadHolderField.getMessageType().findFieldByName("value");

        DynamicMessage.Builder anydataBuilder = DynamicMessage.newBuilder(payloadHolderField.getMessageType());
        anydataBuilder.setField(valueField, "{\"payload-anydata:payload-root\":{\"value\":\"abc\"}}");

        DynamicMessage.Builder wrapperBuilder = DynamicMessage.newBuilder(wrapperDescriptor);
        wrapperBuilder.setField(payloadHolderField, anydataBuilder.build());

        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(
                        new QName("urn:test:outer-anydata", "payload-holder"),
                        payloadSchemaContext);

        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        ContainerData containerData = (ContainerData) YangDataProtoCodec
                .getInstance(wrapperContainer)
                .deserialize(wrapperBuilder.build(), validatorBuilder, options);

        AnyDataData anyDataData = (AnyDataData) containerData.getDataChildren().get(0);
        System.out.println(anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
        // expected output: value
    }
}
```

Expected test YANG files:

```yang
module outer-anydata {
  yang-version 1.1;
  namespace "urn:test:outer-anydata";
  prefix outer;

  container anydata-wrapper {
    anydata payload-holder;
  }
}
```

```yang
module payload-anydata {
  yang-version 1.1;
  namespace "urn:test:payload-anydata";
  prefix payload;

  container payload-root {
    leaf value {
      type string;
    }
  }
}
```

## Technical Highlights

### Design Patterns Used

1. **Factory Pattern** - getInstance() methods for singleton access
2. **Singleton Pattern** - ProtoDescriptorManager, ProtoCache with double-checked locking
3. **Strategy Pattern** - Different codec classes handle different data types
4. **Cache Pattern** - LRU + TTL combination caching strategy

### Thread Safety

- All cache operations are thread-safe
- ConcurrentHashMap for concurrent access
- Volatile and synchronized blocks for singleton pattern

### Error Handling

- Null pointer checks throughout all methods
- Returns default values or null in exceptional cases
- Try-catch blocks protect critical logic

### Type Safety

- Type inference support (infers type from value when type info unavailable)
- Boolean and Number types preserve original type instead of converting to string
- Proper handling of unsigned integers

## Testing

### Test Classes

Current regression coverage includes these active test groups:

1. **AnydataValidationOptionsProtoCodecTest**
   - `anydata` payload schema-resolution behavior for the module-specific JSON-text wrapper path

2. **ProtoCodecDataTest**
   - container sanity checks
   - keyed `List` descriptor generation and SIMPLE/YGOT round-trip coverage
   - repeated `leaf-list` ordering and repeated keyed `list` entry coverage inside `repeated-container`
   - basic leaf-list error-path contract checks

3. **ProtoCodecModeTest**
   - SIMPLE vs YGOT mapping, wrapper, numbering, and descriptor behavior

4. **ProtoCodecStructureTest**
   - `YangStructure` descriptor generation
   - SIMPLE/YGOT structure round-trip coverage

5. **RpcProtoCodecTest**
   - RPC / input / output descriptor generation
   - SIMPLE/YGOT round-trip coverage for simple RPC paths
   - SIMPLE/YGOT nested-container round-trip coverage for complex RPC input/output

### Current Module Test Snapshot

The current module test snapshot observed in this workspace is:

```
Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

This provides evidence for:
- ✅ Core type mapping behavior
- ✅ Descriptor generation for container, list, RPC, and `YangStructure` paths covered by the active tests
- ✅ SIMPLE/YGOT round-trip behavior for selected `List`, repeated-child container, RPC, and `YangStructure` scenarios
- ✅ Module-specific `anydata` wrapper handling and validation-option flow

## Build Output

```
[INFO] Building jar: yangkit-data-proto-codec-1.5.0.jar (45.2KB)
[INFO] Building jar: yangkit-data-proto-codec-1.5.0-sources.jar (28.7KB)
[INFO] Building jar: yangkit-data-proto-codec-1.5.0-javadoc.jar (184.5KB)
```

## Performance Optimizations

### Caching Strategy

1. **Descriptor Cache** - ProtoSchemaGenerator internally caches generated message descriptors
2. **Manager Cache** - ProtoDescriptorManager maintains global descriptor cache
3. **General Cache** - ProtoCache provides general-purpose caching services

### Best Practices

1. **Reuse Generators** - ProtoSchemaGenerator instances should be reused when possible
2. **Leverage Caching** - Frequently used descriptors are automatically cached
3. **Batch Processing** - When encoding/decoding multiple related nodes, get parent descriptor first
4. **Clean Cache** - Periodically call clearCache() to release unused resources

## Future Enhancements

Potential areas for extension:

1. **Complete Descriptor Creation** - Implement createDescriptor method to generate complete Protobuf descriptors
2. **Stream Processing** - Support streaming codec for large YANG data
3. **Async Codec** - Use CompletableFuture for improved concurrent performance
4. **More Type Support** - Enhance mapping for complex types like union, identityref
5. **Schema Validation** - Integrate YANG constraint validation during codec process

## Dependencies

Main dependencies:
- Google Protocol Buffers (protobuf-java)
- yangkit-model-api (YANG model API)
- yangkit-data-api (YANG data API)
- JUnit (testing framework)

## Conclusion

The `yangkit-data-proto-codec` module provides a usable, extensible YANG-to-Protobuf conversion stack with test evidence for core type/value conversion, descriptor generation helpers, module-specific `anydata` wrapper handling, keyed `List` round-trips, repeated child collection handling, RPC descriptor/input/output coverage, and `YangStructure` descriptor plus SIMPLE/YGOT round-trip paths. Additional interoperability depth and broader schema coverage remain active enhancement areas.
