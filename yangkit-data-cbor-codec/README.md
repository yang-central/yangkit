# yangkit-data-cbor-codec

CBOR codec for YANG data based on [RFC 9254](https://datatracker.ietf.org/doc/html/rfc9254).

## Overview

This module provides serialization and deserialization of YANG data to/from CBOR (Concise Binary Object Representation) format.
It implements the specification defined in RFC 9254 - "YANG Data Model in Concise Binary Object Representation (CBOR)".

## Features

- **Binary encoding**: Compact binary representation of YANG data
- **Type-safe conversion**: Automatic mapping between YANG types and CBOR types
- **RFC 9254 compliant**: Follows the standard for YANG data in CBOR
- **Complete coverage**: Supports all YANG data node types:
  - Container nodes
  - Leaf nodes
  - Leaf-list nodes
  - List nodes
  - AnyData nodes
  - AnyXML nodes
  - RPC/Action nodes
  - Notification nodes

## Dependencies

Add this dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-cbor-codec</artifactId>
    <version>1.5.0</version>
</dependency>
```

## Usage

### Serialize YANG Data to CBOR

```java
import org.yangcentral.yangkit.data.codec.cbor.*;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.data.api.model.ContainerData;

// Get schema context
YangSchemaContext schemaContext = YangYinParser.parse("module.yang");

// Get container schema
Container container = schemaContext.getModules().get(0).getDataNodeChildren().get(0);

// Create CBOR codec
ContainerDataCborCodec cborCodec = new ContainerDataCborCodec(container);

// Serialize YANG data to CBOR bytes
byte[] cborBytes = cborCodec.serialize(containerData);
```

### Deserialize CBOR to YANG Data

```java
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;

// Create validator result builder
ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

// Deserialize CBOR bytes to YANG data
ContainerData data = cborCodec.deserialize(cborBytes, validatorResultBuilder);
```

### Working with Different Data Types

#### Leaf Data

```java
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.data.api.model.LeafData;

Leaf leaf = ...; // Get leaf schema
LeafDataCborCodec leafCodec = new LeafDataCborCodec(leaf);

// Serialize
byte[] cborData = leafCodec.serialize(leafData);

// Deserialize
LeafData<?> data = leafCodec.deserialize(cborData, validatorResultBuilder);
```

#### Leaf-List Data

```java
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.data.api.model.LeafListData;

LeafList leafList = ...; // Get leaf-list schema
LeafListDataCborCodec leafListCodec = new LeafListDataCborCodec(leafList);

// Serialize
byte[] cborData = leafListCodec.serialize(leafListData);

// Deserialize
LeafListData<?> data = leafListCodec.deserialize(cborData, validatorResultBuilder);
```

#### List Data

```java
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.data.api.model.ListData;

YangList list = ...; // Get list schema
ListDataCborCodec listCodec = new ListDataCborCodec(list);

// Serialize
byte[] cborData = listCodec.serialize(listData);

// Deserialize
ListData data = listCodec.deserialize(cborData, validatorResultBuilder);
```

## CBOR Encoding Rules

The module follows RFC 9254 encoding rules:

- **Strings**: Encoded as CBOR text strings
- **Numbers**: Encoded as CBOR integers or floating-point numbers based on type
- **Booleans**: Encoded as CBOR boolean values
- **Binary data**: Encoded as CBOR byte strings
- **Containers**: Encoded as CBOR maps
- **Lists/Leaf-lists**: Encoded as CBOR arrays
- **Null values**: Encoded as CBOR null

## Architecture

The module uses a layered architecture:

1. **Base Layer**: `YangDataCborCodec` - Abstract base class providing common functionality
2. **Codec Layer**: Specific codecs for each YANG data type (Container, Leaf, LeafList, List, etc.)
3. **Utility Layer**: `CborCodecUtil` - Helper methods for type conversion
4. **Exception Layer**: `YangDataCborCodecException` - Exception handling

## Implementation Status

✅ Core infrastructure (base classes, utilities, exceptions)
✅ Container data codec
✅ Leaf data codec
✅ Leaf-list data codec
✅ List data codec
⏳ AnyData/AnyXML codec (basic implementation)
⏳ RPC/Notification codec (basic implementation)

## References

- [RFC 9254 - YANG Data Model in CBOR](https://datatracker.ietf.org/doc/html/rfc9254)
- [RFC 7049 - Concise Binary Object Representation (CBOR)](https://datatracker.ietf.org/doc/html/rfc7049)
- [Jackson CBOR](https://github.com/FasterXML/jackson-dataformats-binary/tree/master/cbor)
- [YANG RFC 7950](https://datatracker.ietf.org/doc/html/rfc7950)

## License

Apache License, Version 2.0
