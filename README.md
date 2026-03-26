# Yangkit

## overview

Yangkit is a toolkit for YANG([RFC7950](https://datatracker.ietf.org/doc/html/rfc7950)) data model language including YANG parser, XPath evaluator, data codecs (JSON, XML, Protocol Buffers), and other tools.

## Yangkit components
* [yangkit-parser](yangkit-parser/README.md): Parse YANG files and validate the parsed YANG modules
* [yangkit-model-api](yangkit-model-api/README.md): The APIs for YANG model
* [yangkit-model-impl](yangkit-model-impl/README.md): The implementations for YANG model
* [yangkit-xpath-api](yangkit-xpath-api/README.md): The APIs for YANG XPATH parser, validator and evaluator.
* [yangkit-xpath-impl](yangkit-xpath-impl/README.md): The Implementations for YANG XPATH parser, validator and evaluator.
* [yangkit-data-api](yangkit-data-api/README.md): The APIs for YANG data representation and operation.
* [yangkit-data-impl](yangkit-data-impl/README.md): The implementations for YANG data representation and operation.
* [yangkit-data-json-codec](yangkit-data-json-codec/README.md): JSON codec for YANG data (serialization/deserialization)
* [yangkit-data-xml-codec](yangkit-data-xml-codec/README.md): XML codec for YANG data (serialization/deserialization)
* [yangkit-data-proto-codec](yangkit-data-proto-codec/README.md): Protocol Buffers codec for YANG data (serialization/deserialization)
* [yangkit-data-cbor-codec](yangkit-data-cbor-codec/README.md): CBOR codec for YANG data based on RFC 9254 - **NEW**
* [yangkit-examples](yangkit-examples/README.md): Example applications demonstrating how to use Yangkit

## Installation
### From source
```bash
git clone https://github.com/yang-central/yangkit.git
cd yangkit
mvn clean install
```

### Maven dependency

#### yangkit-parser
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-parser</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-model-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-api</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-model-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-impl</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-xpath-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-xpath-api</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-xpath-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-xpath-impl</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-api</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-impl</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-json-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-json-codec</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-xml-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-xml-codec</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-proto-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-proto-codec</artifactId>
    <version>1.5.0</version>
</dependency>
```

#### yangkit-data-cbor-codec (NEW)
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-cbor-codec</artifactId>
    <version>1.5.0</version>
</dependency>
```

## Key Features

### 🎯 Core Capabilities
- **YANG Parser**: Full support for RFC 7950 YANG grammar with ANTLR4
- **XPath Evaluator**: Complete XPath 1.0 implementation for YANG data validation
- **Data Validation**: Comprehensive validation against YANG schemas
- **Multiple Codecs**: 
  - JSON encoding/decoding (RFC 7951)
  - XML encoding/decoding (RFC 7950 Section 8)
  - Protocol Buffers encoding/decoding
  - CBOR encoding/decoding (RFC 9254) - NEW in 1.5.0

### 🆕 New in Version 1.5.0 - Proto Codec Module
The `yangkit-data-proto-codec` module provides efficient Protocol Buffers serialization/deserialization for YANG data:

- **Type-safe conversion**: Automatic mapping between YANG types and Protobuf types
- **Complete coverage**: Supports all YANG data node types (Container, List, Leaf, LeafList, AnyData, AnyXML, RPC, Notification, etc.)
- **Schema-driven**: Generates Protobuf descriptors from YANG schema
- **High performance**: Leverages Protobuf's binary format for compact representation

**Example usage:**
```java
// Get schema context
YangSchemaContext schemaContext = YangYinParser.parse("module.yang");

// Get container schema
Container container = schemaContext.getModules().get(0).getDataNodeChildren().get(0);

// Create protobuf codec
ContainerDataProtoCodec protoCodec = new ContainerDataProtoCodec(container);

// Serialize YANG data to Protobuf
DynamicMessage protoMessage = protoCodec.serialize(containerData);

// Deserialize Protobuf to YANG data
ContainerData data = protoCodec.deserialize(protoMessage, validatorResultBuilder);
```

## Documentation
Please see the JavaDoc in each component or visit:
- [API Documentation](apidocs/index.html)
- [Examples](yangkit-examples)