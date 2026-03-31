# yangkit-data-api

## Overview

`yangkit-data-api` provides the core APIs for reading, building, validating, and manipulating YANG data trees.

## Features

- Access YANG data nodes and documents
- Create, delete, merge, replace, and update YANG data
- Work with typed nodes such as container, list, leaf, leaf-list, anydata, and anyxml
- Share common codec-facing abstractions across XML, JSON, CBOR, and Protocol Buffers modules
- Provide document-level anydata validation context APIs

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-api</artifactId>
    <version>1.6.0</version>
</dependency>
```

## Anydata Validation Context API

This module defines the common API used by the XML, JSON, CBOR, and Protocol Buffers codecs to resolve the payload schema of `anydata` nodes during deserialization.

### Main Types

- `AnydataValidationContext`
  - Represents the validation context for one `anydata` payload
  - Usually wraps a `YangSchemaContext`
- `DefaultAnydataValidationContext`
  - Default implementation backed by a `YangSchemaContext`
- `AnydataValidationRequest`
  - Carries runtime matching information for one `anydata` node
  - Includes:
    - schema node
    - schema node identifier
    - source path
    - document schema context
- `AnydataValidationContextResolver`
  - Strategy interface used by codecs to resolve the payload validation context
- `AnydataValidationOptions`
  - Built-in resolver implementation with common matching rules

### Matching Order in `AnydataValidationOptions`

When codecs resolve an `anydata` payload schema with `AnydataValidationOptions`, the matching order is:

1. `addRule(...)`
2. `registerSchemaContext(...)` / `registerContext(...)`
3. `defaultSchemaContext(...)` / `defaultContext(...)`

That means a rule-based match has higher priority than a schema-node-identifier match.

### Typical Usage

```java
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext outerSchemaContext = ...;
YangSchemaContext payloadSchemaContext = ...;

AnydataValidationOptions options = new AnydataValidationOptions()
        .registerSchemaContext(
                new QName("urn:test:outer-anydata", "payload-holder"),
                payloadSchemaContext)
        .addRule(
                request -> request != null
                        && request.getSourcePath() != null
                        && request.getSourcePath().contains("payload-holder"),
                payloadSchemaContext)
        .defaultSchemaContext(outerSchemaContext);
```

### Complete Minimal Example

The following example shows the smallest complete setup pattern used by the codec modules.
It loads an outer schema, loads an embedded payload schema, validates both, and prepares one reusable `AnydataValidationOptions` instance.

```java
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangYinParser;

public class AnydataOptionsExample {
    public static void main(String[] args) throws Exception {
        YangSchemaContext outerSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/outer/yang");
        YangSchemaContext payloadSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/payload/yang");

        if (!outerSchemaContext.validate().isOk() || !payloadSchemaContext.validate().isOk()) {
            throw new IllegalStateException("schema validation failed");
        }

        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(
                        new QName("urn:test:outer-anydata", "payload-holder"),
                        payloadSchemaContext)
                .addRule(
                        request -> request != null
                                && request.getSourcePath() != null
                                && request.getSourcePath().contains("payload-holder"),
                        payloadSchemaContext)
                .defaultSchemaContext(outerSchemaContext);

        System.out.println("Anydata validation options prepared: " + options);
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

### Expected Behavior Without a Matching Context

If no matching payload schema context is found for an `anydata` node, the codecs still create the `anydata` node itself.
The payload document may deserialize with zero recognized data children when the outer schema cannot describe the embedded payload.

## Related Codec Modules

- `../yangkit-data-xml-codec/README.md`
- `../yangkit-data-json-codec/README.md`
- `../yangkit-data-cbor-codec/README.md`
- `../yangkit-data-proto-codec/README.md`

## Documentation

[Javadoc](apidocs/index.html)