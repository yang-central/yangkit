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

### Anydata Payload Representation

For `anydata` nodes, the current protobuf codec generates a wrapper protobuf message for the `anydata` schema node.
Its payload is carried in a `value` field as JSON text, and deserialization resolves the payload schema through `AnydataValidationOptions`.

This design keeps protobuf transport compatible with the existing JSON document parsing logic used by the other codecs.

Typical behavior:

- outer protobuf message identifies the `anydata` schema node
- embedded JSON payload is read from the generated wrapper message
- payload schema is resolved by rule, schema-node registration, or default context
- if no context matches, the `anydata` node still exists but its payload may have zero recognized children

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
