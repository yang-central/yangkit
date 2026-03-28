# yangkit-data-proto-codec

Protocol Buffers codec for YANG data - provides bidirectional conversion between YANG data and Protocol Buffers messages.

**Note**: This module is fully compatible with [openconfig ygot](https://github.com/openconfig/ygot) protobuf naming conventions and type mappings.

## Overview

This module implements the `YangDataCodec` interface to enable serialization and deserialization of YANG data to/from Protocol Buffers format, similar to the JSON and XML codecs in this project.

## Features

- Bidirectional conversion between YANG data and Protocol Buffers messages
- **ygot-compatible** type mappings and naming conventions
- Support for all YANG data node types:
  - Container
  - List
  - Leaf
  - Leaf-List
  - Anydata
  - Anyxml
  - Notification
  - RPC (Input/Output)
  - Action
  - YangStructure

## Architecture

The codec follows the same pattern as `yangkit-data-json-codec`:

1. **YangDataProtoCodec** - Abstract base class providing common functionality
2. **ProtoCodecUtil** - Utility class with conversion methods
3. **Concrete codecs** - Specific implementations for each YANG data type
4. **YangDataProtoCodecException** - Exception class for codec errors

## Usage

### Serialize YANG Data to Protocol Buffers

```java
import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

// Get schema node
SchemaNode schemaNode = ...;

// Get existing YANG data
YangData<?> yangData = ...;

// Serialize to protobuf
DynamicMessage protoMessage = YangDataProtoCodec
    .getInstance(schemaNode)
    .serialize(yangData);
```

### Deserialize Protocol Buffers to YANG Data

```java
import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

// Get schema node
SchemaNode schemaNode = ...;

// Get protobuf message
DynamicMessage protoMessage = ...;

// Deserialize to YANG data
ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
YangData<?> yangData = YangDataProtoCodec
    .getInstance(schemaNode)
    .deserialize(protoMessage, validatorBuilder);
```

## Implementation Details

### Type Mapping

The codec handles conversion between YANG types and Protocol Buffer types with full compatibility to openconfig ygot specifications:

| YANG Type | Protobuf Type | Notes |
|-----------|---------------|-------|
| int8, int16, int32 | int32 | |
| int64 | int64 | |
| uint8, uint16, uint32 | uint32 | |
| uint64 | uint64 | |
| string | string | |
| boolean | bool | |
| decimal64 | **string** | Mapped to string to preserve precision (ygot compatible) |
| binary | bytes | |
| enumeration | enum/string | |
| empty | bool | |
| date-and-time | int64 | Unix timestamp in milliseconds |
| date-only | int32 | Days since epoch |
| time-of-day | int64 | Milliseconds since midnight |

### Descriptor Generation

To convert between YANG and Protocol Buffers, protobuf descriptors need to be generated from YANG schemas. This can be done using:

1. Dynamic protobuf descriptors generated at runtime
2. Pre-compiled protobuf definitions that match the YANG schema

### Naming Conventions

All protobuf message and field names follow ygot-compatible naming conventions:

- **Message Names**: PascalCase (e.g., `TpContainer`, `LeafList`)
  - Converts YANG names: `tp-container` → `TpContainer`
  
- **Field Names**: snake_case (e.g., `string_leaf`, `nested_container`)
  - Converts YANG names: `string-leaf` → `string_leaf`
  
- **RPC Messages**: PascalCase with Input/Output suffixes
  - Example: `get-info` RPC → `GetInfoInput` / `GetInfoOutput`
  
- **Package Names**: lowercase with underscores
  - Example: `test-proto` module → `yangkit.proto.test_proto`

## Dependencies

- yangkit-data-api
- yangkit-data-impl
- yangkit-parser
- protobuf-java
- protobuf-java-util

## Future Enhancements

Potential areas for enhancement:

1. Complete implementation of all serialize/deserialize methods
2. Add support for YANG extensions in protobuf
3. Optimize performance for large data sets
4. Add comprehensive unit tests
5. Support for protobuf oneof fields for YANG choice/case
6. Handle YANG metadata and annotations in protobuf

## References

- RFC 7950 - The YANG 1.1 Data Modeling Language
- Protocol Buffers Documentation: https://developers.google.com/protocol-buffers
- YANG Kit Project: https://github.com/yang-central/yangkit
