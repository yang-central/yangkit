# yangkit-data-proto-codec

Protocol Buffers codec for YANG data - provides the current bidirectional conversion paths between YANG data and the generated Protocol Buffers messages/descriptors used by this module.

**Note**: This module supports two protobuf codec modes: `SIMPLE` (default, primitive proto3 fields) and `YGOT` (ygot-style scalar wrappers and hash-based field numbers).

## Overview

This module implements the `YangDataCodec` interface to enable the current serialization and deserialization paths between YANG data and the generated Protocol Buffers structures used in this project, similar to the JSON and XML codecs.

## Features

- Bidirectional conversion paths between YANG data and generated Protocol Buffers messages used by this module
- Support for both `SIMPLE` and `YGOT` schema generation modes in the current generator/codec stack
- ygot-style naming conventions and scalar wrapper support in `YGOT` mode
- Codec classes currently exist for these YANG node categories:
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
- `anydata` payloads are currently serialized as JSON text inside a generated wrapper message's `value` field

Coverage depth and interoperability are not identical for every node category. Current regression coverage now includes:

- `YangStructure`: descriptor generation plus SIMPLE/YGOT round-trip regression coverage
- `List`: keyed-entry descriptor generation plus SIMPLE/YGOT round-trip coverage, including restored list-key handling in the current proto path
- repeated child collections inside `repeated-container`: SIMPLE/YGOT coverage for repeated `leaf-list` ordering and repeated keyed `list` entry round-trips
- `RPC`: descriptor coverage for RPC / input / output, SIMPLE/YGOT round-trip coverage for the simple `get-info` RPC, and SIMPLE/YGOT nested-container round-trip coverage for `update-config`

These checks are tied to the generated descriptor/message shapes used by this module. They should not be read as blanket protobuf interoperability guarantees for every external schema or consumer.

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

### Deserialize Protocol Buffers with `anydata` validation options

```java
import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

SchemaNode schemaNode = ...;
DynamicMessage protoMessage = ...;
YangSchemaContext payloadSchemaContext = ...;

AnydataValidationOptions options = new AnydataValidationOptions()
        .registerSchemaContext(
                new QName("urn:test:outer-anydata", "payload-holder"),
                payloadSchemaContext);

ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
YangData<?> yangData = YangDataProtoCodec
        .getInstance(schemaNode)
        .deserialize(protoMessage, validatorBuilder, options);
```

## Implementation Details

### Type Mapping

The codec handles conversion between YANG types and Protocol Buffer types according to the selected `ProtoCodecMode`:

| YANG Type | `SIMPLE` mode | `YGOT` mode | Notes |
|-----------|---------------|-------------|-------|
| int8, int16, int32 | `int32` | `.ywrapper.IntValue` | `IntValue.value` uses `sint64` |
| int64 | `int64` | `.ywrapper.IntValue` | |
| uint8, uint16, uint32 | `uint32` | `.ywrapper.UintValue` | `UintValue.value` uses `uint64` |
| uint64 | `uint64` | `.ywrapper.UintValue` | |
| string | `string` | `.ywrapper.StringValue` | |
| boolean | `bool` | `.ywrapper.BoolValue` | |
| decimal64 | `string` | `.ywrapper.Decimal64Value` | `Decimal64Value` stores `digits:uint64` + `precision:uint32` |
| binary | `bytes` | `.ywrapper.BytesValue` | |
| enumeration / bits | generated enum | generated enum | Not wrapped in `ywrapper` |
| empty | `bool` | `.ywrapper.BoolValue` | Presence/absence semantics |
| identityref | `string` | `.ywrapper.StringValue` | QName string form |
| union / leafref | `string` | generated `oneof` / wrapper-backed message fields | Mode-specific schema generation |

Types not recognized by the built-in mapper fall back to `string` handling unless a caller normalizes them before encoding.

`SIMPLE` mode is the default for `ProtoSchemaGenerator` and `YangDataProtoCodec.getInstance(schemaNode)`. Use `ProtoCodecMode.YGOT` when you want ywrapper-based scalar fields and ygot-style field numbering.

### Descriptor Generation

To convert between YANG and Protocol Buffers in this module, protobuf descriptors need to be generated from YANG schemas. This can be done using:

1. Dynamic protobuf descriptors generated at runtime
2. Pre-compiled protobuf definitions that intentionally mirror the schema shape generated by this module

### Anydata Payload Representation

For `anydata` nodes, the current protobuf codec generates a wrapper protobuf message for the `anydata` schema node.
Its payload is carried in a `value` field as JSON text, and deserialization resolves the payload schema through `AnydataValidationOptions`.

This design reuses the existing JSON document parsing logic used by the other codecs; it does not imply protobuf-level wire compatibility with other `anydata` representations.

Typical behavior:

- outer protobuf message identifies the `anydata` schema node
- embedded JSON payload is read from the generated wrapper message
- payload schema is resolved by rule, schema-node registration, or default context
- if no context matches, the `anydata` node still exists but its payload may have zero recognized children

### Naming Conventions

Generated protobuf message and field names currently use ygot-style-inspired naming conventions in the schema generator:

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
- yangkit-data-json-codec
- yangkit-parser
- protobuf-java
- protobuf-java-util

## Notes

- `AnydataValidationOptions` matching order is: rule > schema-node registration > default context
- For `anydata`, protobuf deserialization now participates in the same document-level payload schema resolution model as XML, JSON, and CBOR
- Rule-based matching can use `request.getSourcePath()` when the same schema node may carry different payload schema sets in different locations

## Complete Minimal Runnable Example

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


## Future Enhancements

Potential areas for further enhancement:

1. Add support for YANG extensions in protobuf
2. Optimize performance for very large data sets
3. Support protobuf `oneof` generation for YANG choice/case
4. Handle YANG metadata and annotations in protobuf more explicitly

## References

- RFC 7950 - The YANG 1.1 Data Modeling Language
- Protocol Buffers Documentation: https://developers.google.com/protocol-buffers
- YANG Kit Project: https://github.com/yang-central/yangkit
