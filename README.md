# Yangkit

## overview

Yangkit is a toolkit for YANG([RFC7950](https://datatracker.ietf.org/doc/html/rfc7950)) data model language including a YANG parser, XPath evaluator, data APIs, and data codecs for JSON, XML, Protocol Buffers, and CBOR.

Yangkit currently provides implemented and tested support for major parts of RFC 7950/6020, RFC 7951 JSON processing, RFC 8791 structure handling in selected paths, and RFC 9254 CBOR encoding.

## Yangkit components
* [yangkit-parser](yangkit-parser/README.md): Parse YANG files and validate the parsed YANG modules
* [yangkit-model-api](yangkit-model-api/README.md): The APIs for YANG model
* [yangkit-model-impl](yangkit-model-impl/README.md): The implementations for YANG model
* [yangkit-xpath-api](yangkit-xpath-api/README.md): The APIs for YANG XPATH parser, validator and evaluator.
* [yangkit-xpath-impl](yangkit-xpath-impl/README.md): The Implementations for YANG XPATH parser, validator and evaluator.
* [yangkit-data-api](yangkit-data-api/README.md): Shared YANG data abstractions and operations.
* [yangkit-data-impl](yangkit-data-impl/README.md): Implementations for YANG data representation and operation.
* [yangkit-data-json-codec](yangkit-data-json-codec/README.md): JSON codec for YANG data.
* [yangkit-data-xml-codec](yangkit-data-xml-codec/README.md): XML codec for YANG data.
* [yangkit-data-proto-codec](yangkit-data-proto-codec/README.md): Protocol Buffers codec for YANG data.
* [yangkit-data-cbor-codec](yangkit-data-cbor-codec/README.md): CBOR codec for YANG data based on RFC 9254.
* [yangkit-examples](yangkit-examples): Example applications demonstrating how to use Yangkit.

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
    <version>1.6.0</version>
</dependency>
```

#### yangkit-model-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-api</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-model-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-impl</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-xpath-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-xpath-api</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-xpath-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-xpath-impl</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-api
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-api</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-impl
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-impl</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-json-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-json-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-xml-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-xml-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-proto-codec
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-proto-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

#### yangkit-data-cbor-codec (NEW)
```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-cbor-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

## Key Features

### 🎯 Core Capabilities
- **YANG Parser**: Core RFC 7950 / RFC 6020 parsing and validation capabilities with targeted regression coverage
- **XPath Evaluator**: Complete XPath 1.0 implementation for YANG data validation
- **Data Validation**: Comprehensive validation against YANG schemas
- **Multiple Codecs**: 
  - JSON encoding/decoding with tested RFC 7951 coverage for core scenarios
  - XML encoding/decoding with tested RFC 7950 Section 8 scenarios
  - Protocol Buffers encoding/decoding
  - CBOR encoding/decoding with tested RFC 9254 coverage for core scenarios - NEW in 1.5.0

### 🆕 New in Version 1.6.0
Version 1.6.0 includes bug fixes and improvements:

- 🔧 Fixed XML codec type validation issues
- 📝 Improved test coverage for all codecs
- 🐛 Fixed YANG module naming conventions
- ✨ Enhanced schema node lookup in XML codec
- 🔄 Added shared `anydata` validation-context support across XML, JSON, Protocol Buffers, and CBOR codecs
- 📚 Added comprehensive developer guide


## Documentation
Please see the JavaDoc in each component or visit:
- Module-specific API documentation under each component's `apidocs/` directory
- [Examples](yangkit-examples)
