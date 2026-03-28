# YangKit Developer Guide

## Table of Contents

1. [Project Overview](#project-overview)
2. [Quick Start](#quick-start)
3. [Architecture Design](#architecture-design)
4. [Core Modules Details](#core-modules-details)
5. [Development Environment Setup](#development-environment-setup)
6. [Build and Deployment](#build-and-deployment)
7. [Coding Standards](#coding-standards)
8. [Testing Standards](#testing-standards)
9. [FAQ](#faq)
10. [Contributing Guide](#contributing-guide)

---

## Project Overview

### Introduction

YangKit is a powerful YANG modeling language toolkit with full support for [RFC 7950](https://datatracker.ietf.org/doc/html/rfc7950) and [RFC 6020](https://datatracker.ietf.org/doc/html/rfc6020) standards. It provides a complete solution from YANG file parsing and XPath evaluation to multiple data encoding/decoding formats.

### Key Features

- **Complete YANG Parser**: Based on ANTLR4, supports YANG 1.1 version
- **XPath Evaluator**: Full XPath 1.0 implementation for YANG data validation
- **Multiple Encoding Formats**:
  - JSON (RFC 7951)
  - XML (RFC 7950 Section 8)
  - Protocol Buffers
  - CBOR (RFC 9254) - New in v1.5.0
- **Data Validation**: Comprehensive validation based on YANG schema
- **Modular Design**: Clear API/Impl separation architecture

### Version Information

- **Current Version**: 1.6.0
- **Java Version**: Java 8+
- **Build Tool**: Maven 3.6+
- **License**: Apache License 2.0

---

## Quick Start

### Installation

#### Build from Source

```bash
git clone https://github.com/yang-central/yangkit.git
cd yangkit
mvn clean install
```

#### Maven Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<!-- Basic dependencies -->
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-parser</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-api</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-model-impl</artifactId>
    <version>1.6.0</version>
</dependency>

<!-- Data codecs (choose as needed) -->
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-json-codec</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-xml-codec</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-proto-codec</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-cbor-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

### Hello World Example

```java
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import com.google.gson.Gson;

public class HelloWorld {
    public static void main(String[] args) throws Exception {
        // 1. Parse YANG file
        Module module = YangYinParser.parse("path/to/module.yang");
        
        // 2. Get container schema
        Container container = module.getDataNodeChildren().get(0);
        
        // 3. Create JSON codec
        ContainerDataJsonCodec jsonCodec = new ContainerDataJsonCodec(container);
        
        // 4. Deserialize JSON data
        String json = "{\"my-container\": {\"leaf\": \"value\"}}";
        YangData<?> data = jsonCodec.deserialize(json);
        
        System.out.println("Successfully parsed YANG data!");
    }
}
```

---

## Architecture Design

### Layered Architecture

YangKit adopts a clear layered architecture design:

```
┌─────────────────────────────────────┐
│         Application Layer           │
│    (Your Application Code)          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         Codec Layer                 │
│  (JSON/XML/Proto/CBOR Codecs)       │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Data Model Layer               │
│    (yangkit-data-impl)              │
└─────────────────────────────────────┐
              ↓
┌─────────────────────────────────────┐
│     YANG Model Layer                │
│   (yangkit-model-impl)              │
└─────────────────────────────────────┐
              ↓
┌─────────────────────────────────────┐
│      Parser Layer                   │
│     (yangkit-parser)                │
└─────────────────────────────────────┘
```

### Module Dependencies

```
yangkit-examples
    ├── yangkit-data-json-codec
    ├── yangkit-data-xml-codec
    ├── yangkit-data-proto-codec
    └── yangkit-data-cbor-codec
            ↓
    yangkit-data-impl
            ↓
    yangkit-model-impl ← yangkit-xpath-impl
            ↓                   ↓
    yangkit-data-api    yangkit-xpath-api
            ↓                   ↓
    yangkit-model-api ← yangkit-parser
```

### Design Patterns

1. **API/Impl Separation**: Each module has independent API and implementation packages
2. **Factory Pattern**: Use factory classes to create instances (e.g., `YangDataBuilderFactory`)
3. **Strategy Pattern**: Different codec implementations share a unified interface
4. **Builder Pattern**: Use Builder pattern to construct complex objects

---

## Core Modules Details

### 1. yangkit-parser

**Functionality**: YANG file parser

**Core Classes**:
- `YangYinParser`: Main parser entry
- `YangLexer`: Lexer
- `YangParser`: Parser

**Usage Example**:
```java
// Parse single YANG file
Module module = YangYinParser.parse("module.yang");

// Parse all YANG files in directory
YangSchemaContext context = YangYinParser.parse("/path/to/yang/files");

// Get modules
List<Module> modules = context.getModules();
```

### 2. yangkit-model-api & yangkit-model-impl

**Functionality**: API definition and implementation of YANG model elements

**Core Interfaces/Classes**:
- `Module`: YANG module
- `Container`: Container node
- `Leaf`: Leaf node
- `LeafList`: Leaf list
- `List`: List node
- `Choice`: Choice node
- `Case`: Case node
- `AnyData`: Any data node
- `Rpc`: RPC operation
- `Notification`: Notification node

**Type System**:
```java
// Get leaf type
Leaf leaf = ...;
TypeDefinition typeDef = leaf.getType();

// Get type constraints
LengthConstraint length = typeDef.getLength();
PatternConstraint pattern = typeDef.getPattern();
RangeConstraint range = typeDef.getRange();
```

### 3. yangkit-xpath-api & yangkit-xpath-impl

**Functionality**: XPath parser and evaluator

**Core Classes**:
- `XPathParser`: XPath expression parser
- `XPathEvaluator`: XPath evaluator

**Usage Example**:
```java
// Create XPath evaluator
XPathEvaluator evaluator = new XPathEvaluator();

// Set context
evaluator.setContext(dataNode);

// Evaluate XPath expression
String result = evaluator.evaluate("/container/leaf");

// XPath with variables
evaluator.setVariable("name", "value");
List<Node> nodes = evaluator.evaluate("//leaf[name=$name]");
```

### 4. yangkit-data-api & yangkit-data-impl

**Functionality**: YANG data representation and manipulation

**Core Classes**:
- `YangData`: Data node base class
- `YangDataDocument`: Data document
- `YangDataContainer`: Data container
- `LeafData`: Leaf data
- `ContainerData`: Container data
- `ListData`: List data

**Data Manipulation**:
```java
// Create data
ContainerData containerData = new ContainerDataImpl(container);
LeafData leafData = new LeafDataImpl(leaf, "value");
containerData.addDataChild(leafData);

// Access data
List<YangData<?>> children = containerData.getDataChildren();
YangData<?> child = containerData.getDataChild(identifier);

// Remove data
containerData.removeDataChild(identifier);
```

### 5. yangkit-data-json-codec

**Functionality**: JSON codec (RFC 7951)

**Usage Example**:
```java
// Deserialize
String json = "{\"container\": {\"leaf\": \"value\"}}";
ContainerDataJsonCodec codec = new ContainerDataJsonCodec(container);
ContainerData data = codec.deserialize(json);

// Serialize
String jsonString = codec.serialize(data);
```

### 6. yangkit-data-xml-codec

**Functionality**: XML codec (RFC 7950 Section 8)

**Usage Example**:
```java
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

// Deserialize
String xml = "<container xmlns='urn:example'><leaf>value</leaf></container>";
Document doc = DocumentHelper.parseText(xml);
ContainerDataXmlCodec codec = new ContainerDataXmlCodec(container);
ContainerData data = codec.deserialize(doc);

// Serialize
Element element = codec.serialize(data);
String xmlString = element.asXML();
```

### 7. yangkit-data-proto-codec

**Functionality**: Protocol Buffers codec

**Usage Example**:
```java
import com.google.protobuf.DynamicMessage;

// Create codec
ContainerDataProtoCodec protoCodec = new ContainerDataProtoCodec(container);

// Serialize
DynamicMessage message = protoCodec.serialize(containerData);
byte[] bytes = message.toByteArray();

// Deserialize
DynamicMessage protoMessage = DynamicMessage.parseFrom(descriptor, bytes);
ContainerData data = protoCodec.deserialize(protoMessage, validatorResultBuilder);
```

### 8. yangkit-data-cbor-codec

**Functionality**: CBOR codec (RFC 9254) - New in v1.5.0

**Usage Example**:
```java
// Serialize
ContainerDataCborCodec cborCodec = new ContainerDataCborCodec(container);
byte[] cborBytes = cborCodec.serialize(containerData);

// Deserialize
ContainerData data = cborCodec.deserialize(cborBytes, validatorResultBuilder);
```

---

## Development Environment Setup

### Required Tools

1. **JDK 8+**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **Git**
   ```bash
   git --version
   ```

### IDE Configuration

#### IntelliJ IDEA

1. Import project: `File → Open → Select yangkit directory`
2. Select `Open as Maven Project`
3. Wait for Maven dependencies to download
4. Configure code style: `Settings → Editor → Code Style → Java`
   - Tab size: 3
   - Indent: 3
   - Continuation indent: 6

#### Eclipse

1. Import project: `File → Import → Existing Maven Projects`
2. Select yangkit root directory
3. Select all submodules
4. Complete import

### Build Options

```bash
# Clean and compile
mvn clean compile

# Compile without tests
mvn clean compile -DskipTests

# Compile specific module
mvn clean compile -pl yangkit-parser -am
```

---

## Build and Deployment

### Local Build

```bash
# Full build (with tests)
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run specific test only
mvn clean test -Dtest=XmlCodecDataTestString
```

### Release Build

```bash
# Generate Javadoc
mvn javadoc:javadoc

# Generate source package
mvn source:jar

# Deploy to Maven Central (requires configuration)
mvn clean deploy -P release
```

### Module Build Order

Maven automatically handles module dependency order:

1. yangkit-xpath-api
2. yangkit-model-api
3. yangkit-data-api
4. yangkit-xpath-impl
5. yangkit-model-impl
6. yangkit-parser
7. yangkit-data-impl
8. yangkit-data-json-codec
9. yangkit-data-xml-codec
10. yangkit-data-proto-codec
11. yangkit-data-cbor-codec
12. yangkit-examples

### Troubleshooting

#### Common Issue 1: Compilation Errors

```bash
# Clean and recompile
mvn clean
mvn dependency:purge-local-repository
mvn install
```

#### Common Issue 2: Test Failures

```bash
# View detailed test output
mvn test -X

# Run only failed tests
mvn test -Dtest.failingSince=xxx
```

---

## Coding Standards

### Naming Conventions

#### Class Naming

```java
// Use PascalCase
public class YangDataDocument {}
public class ContainerDataImpl {}

// Interfaces use descriptive names
public interface SchemaNode {}
public interface DataDefinition {}

// Abstract classes use Abstract prefix
public abstract class AbstractYangCodec {}
```

#### Method Naming

```java
// getter/setter
public String getName() { ... }
public void setName(String name) { ... }

// Boolean values use is/has/can prefixes
public boolean isValid() { ... }
public boolean hasChildren() { ... }

// Collection operations use add/remove/get
public void addChild(YangData child) { ... }
public void removeChild(DataIdentifier id) { ... }
public List<YangData> getChildren() { ... }
```

#### Variable Naming

```java
// Use camelCase
String moduleName = "example";
List<Module> modules = new ArrayList<>();

// Constants use uppercase
public static final String VERSION_11 = "1.1";

// Avoid single-letter variables (except loop counters)
for (int i = 0; i < count; i++) { ... }
```

### Code Organization

#### File Structure

```
// 1. Package declaration
package org.yangcentral.yangkit.data.codec;

// 2. Import declarations (alphabetical order)
import java.util.List;
import org.yangcentral.yangkit.model.api.stmt.Container;

// 3. Class declaration
public class ContainerDataCodec {
    
    // 4. Constant declarations
    private static final String TAG = "ContainerDataCodec";
    
    // 5. Field declarations
    private final Container schemaNode;
    
    // 6. Constructors
    public ContainerDataCodec(Container schemaNode) {
        this.schemaNode = schemaNode;
    }
    
    // 7. Public methods
    public YangData deserialize(String data) {
        // Implementation
    }
    
    // 8. Private methods
    private void validate() {
        // Implementation
    }
    
    // 9. Getter/Setter
    public Container getSchemaNode() {
        return schemaNode;
    }
}
```

### Comment Standards

```
/**
 * XML codec for Container data.
 * 
 * <p>Provides serialization and deserialization functionality between Container data nodes and XML format.</p>
 * 
 * @author Frank Feng
 * @since 1.0.0
 */
public class ContainerDataXmlCodec extends TypedDataXmlCodec<Container, ContainerData<?>> {
    
    /**
     * Deserialize XML data to ContainerData object.
     *
     * @param element XML element
     * @param validatorResultBuilder Validation result builder
     * @return Deserialized ContainerData object, null if failed
     * @throws YangDataXmlCodecException When XML format is invalid
     */
    @Override
    protected ContainerData<?> buildData(Element element, 
                                         ValidatorResultBuilder validatorResultBuilder) {
        // Implementation
    }
}
```

### Exception Handling

```java
// Use specific exception types
try {
    YangData<?> data = codec.deserialize(xml);
} catch (YangCodecException e) {
    logger.error("Failed to deserialize data", e);
    throw e;
} catch (IOException e) {
    logger.error("IO error during deserialization", e);
    throw new YangDataXmlCodecException("IO error", e);
}

// Include context information when logging exceptions
if (validationFailed) {
    logger.error("Validation failed for leaf '{}': {}", 
                 leaf.getName(), 
                 value, 
                 exception);
}
```

---

## Testing Standards

### Testing Framework

- **JUnit 5**: Main testing framework
- **AssertJ**: Fluent assertions (optional)
- **Mockito**: Mock objects (if needed)

### Test Directory Structure

```
src/test/
├── java/
│   └── org/yangcentral/yangkit/
│       ├── codec/
│       │   └── xml/
│       │       └── test/
│       │           ├── XmlCodecBasicTest.java
│       │           └── type/
│       │               ├── XmlCodecDataTestString.java
│       │               └── XmlCodecDataTestUint8.java
│       └── parser/
│           └── YangYinParserTest.java
└── resources/
    └── type/
        ├── string/
        │   ├── string.yang
        │   ├── valid1.xml
        │   └── invalid1.xml
        └── uint8/
            ├── uint8.yang
            └── test.xml
```

### Test Case Writing

#### Basic Test Structure

```java
package org.yangcentral.yangkit.data.codec.xml.test.type;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XmlCodecDataTestString {
    
    private static final String YANG_FILE = "type/string/string.yang";
    
    @Test
    public void validNormal() throws Exception {
        XmlCodecTypeTestFunc.expectedNoError(
            "type/string/valid1.xml", 
            YANG_FILE
        );
    }
    
    @Test
    public void invalidPattern() throws Exception {
        XmlCodecTypeTestFunc.expectedError(
            "type/string/invalid1.xml", 
            YANG_FILE
        );
    }
}
```

#### Test Helper Methods

```java
public class XmlCodecTypeTestFunc {
    
    /**
     * Expect deserialization to succeed with no errors
     */
    public static void expectedNoError(String xmlFile, String yangFile) 
            throws Exception {
        // Load YANG schema
        YangSchemaContext schemaContext = loadYangSchema(yangFile);
        
        // Load XML data
        String xmlContent = loadXmlContent(xmlFile);
        
        // Deserialize
        ValidatorResultBuilder builder = new ValidatorResultBuilder();
        YangData<?> data = deserialize(xmlContent, schemaContext, builder);
        
        // Verify
        ValidatorResult result = builder.build();
        assertTrue(result.isOk(), 
                   () -> "Validation should pass but got errors: " + 
                   result.getRecords());
        assertNotNull(data, "Data should not be null");
    }
    
    /**
     * Expect deserialization to fail or validation errors
     */
    public static void expectedError(String xmlFile, String yangFile) 
            throws Exception {
        // Load YANG schema
        YangSchemaContext schemaContext = loadYangSchema(yangFile);
        
        // Load XML data
        String xmlContent = loadXmlContent(xmlFile);
        
        // Deserialize
        ValidatorResultBuilder builder = new ValidatorResultBuilder();
        YangData<?> data = deserialize(xmlContent, schemaContext, builder);
        
        // Verification should fail
        ValidatorResult result = builder.build();
        assertFalse(result.isOk(), 
                    "XML deserialization should fail for invalid data");
    }
}
```

### Test Resource File Standards

#### YANG File Naming

```
{module-name}.yang
Examples:
- string.yang
- uint8.yang
- test-types.yang
```

#### XML File Naming

```
{scenario}{constraint}.xml
Examples:
- valid1.xml          # Valid scenario 1
- validNormal.xml     # Normal valid data
- invalidPattern.xml  # Pattern validation failure
- invalidLength.xml   # Length validation failure
```

#### YANG File Writing Standards

``yang
module test-string-type {
    yang-version 1.1;
    namespace "urn:xml:test:string";
    prefix xst;
    
    // Avoid metadata that may cause validation errors
    // organization, contact, description are optional
    
    revision 2024-03-28 {
        description "Initial version.";
    }
    
    container string-container {
        config false;
        
        // Clear comments explaining test purpose
        leaf normal {
            type string;
        }
        
        leaf pattern {
            type string {
                pattern "[0-9]{3}-[0-9]{4}";
            }
        }
        
        // Note: Use correct range/length syntax
        leaf ranged {
            type uint8 {
                range "10..200";  // Correct
                // range "min 50";  // Wrong! Should be "min..50"
            }
        }
    }
}
```

### Test Coverage Requirements

- **Statement Coverage**: ≥ 80%
- **Branch Coverage**: ≥ 70%
- **Boundary Condition Tests**: Must include minimum, maximum, null values, etc.
- **Negative Tests**: Each validation rule needs corresponding failure test cases

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=XmlCodecDataTestString

# Run specific test method
mvn test -Dtest=XmlCodecDataTestString#invalidPattern

# View test report
mvn surefire-report:report
```

---

## FAQ

### Q1: How to debug YANG parsing errors?

```java
try {
    Module module = YangYinParser.parse("module.yang");
} catch (YangParserException e) {
    System.err.println("Parse error at line " + e.getLine() + 
                       ", column " + e.getColumn());
    System.err.println("Message: " + e.getMessage());
    e.printStackTrace();
}
```

### Q2: How to handle circular dependent YANG modules?

```java
// Use YangSchemaContext to automatically resolve dependencies
YangSchemaContext context = YangYinParser.parse("/path/to/modules");

// Manually add dependent modules
context.addModule(importedModule);
```

### Q3: How to customize type validation?

```java
public class CustomLeafDataCodec extends LeafDataXmlCodec {
    
    @Override
    protected LeafData buildData(Element element, 
                                 ValidatorResultBuilder builder) {
        LeafData leafData = super.buildData(element, builder);
        
        // Add custom validation logic
        if (!customValidate(leafData)) {
            builder.addRecord(new ValidatorRecord.Builder<String, Element>()
                .setSeverity(Severity.ERROR)
                .setErrorMessage("Custom validation failed")
                .build());
        }
        
        return leafData;
    }
}
```

### Q4: Performance optimization suggestions

1. **Reuse codec instances**: Codecs are stateless and can be reused
2. **Batch processing**: Use batch APIs for large amounts of data
3. **Cache Schema**: Avoid parsing the same YANG files repeatedly

```java
// Good practice
Map<String, ContainerDataJsonCodec> codecCache = new ConcurrentHashMap<>();

ContainerDataJsonCodec getCodec(Container schema) {
    return codecCache.computeIfAbsent(
        schema.getIdentifier().toString(),
        key -> new ContainerDataJsonCodec(schema)
    );
}
```

---

## Contributing Guide

### Submission Process

1. **Fork the project**
2. **Create feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Create Pull Request**

### Commit Message Convention

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation update
- `style`: Code style adjustment
- `refactor`: Refactoring
- `test`: Test related
- `chore`: Build/tools related

**Example**:
```
feat(proto-codec): add CBOR codec support

- Implement CBOR serialization/deserialization
- Add RFC 9254 compliance tests
- Update documentation

Closes #123
```

### Code Review Checklist

- [ ] Code follows coding standards
- [ ] Added necessary unit tests
- [ ] All tests pass
- [ ] Updated documentation
- [ ] No breaking backward compatibility
- [ ] Added appropriate comments

---

## Appendix

### A. References

- [RFC 7950 - YANG 1.1](https://datatracker.ietf.org/doc/html/rfc7950)
- [RFC 7951 - JSON Encoding](https://datatracker.ietf.org/doc/html/rfc7951)
- [RFC 9254 - CBOR Encoding](https://datatracker.ietf.org/doc/html/rfc9254)
- [ANTLR4 Documentation](https://www.antlr.org/antlr4-docs/)
- [Protocol Buffers Documentation](https://developers.google.com/protocol-buffers)

### B. Contact Information

- **Project Homepage**: https://github.com/yang-central/yangkit
- **Issue Reporting**: https://github.com/yang-central/yangkit/issues
- **Email**: fengchongllly@gmail.com

### C. Version History

#### v1.6.0 (2024)
- 🔧 Fixed XML codec type validation issues
- 📝 Improved test coverage for all codecs
- 🐛 Fixed YANG module naming conventions
- ✨ Enhanced XML codec schema node lookup
- 📚 Added comprehensive developer guide

#### v1.5.0 (2024)
- ✨ New CBOR codec module
- ✨ Improved Proto codec performance
- 🐛 Fixed XML codec type validation issues
- 📝 Improved test coverage

#### v1.4.0 (2023)
- ✨ New Proto codec module
- 🔧 Optimized XPath evaluator performance

#### v1.0.0 (2022)
- 🎉 Initial release

---

*Last updated: March 28, 2024*

