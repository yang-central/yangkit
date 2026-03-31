# yangkit-data-json-codec

JSON codec for YANG data.

## Overview

This module provides serialization and deserialization between YANG data and JSON documents.
It is typically used together with `yangkit-data-api`, `yangkit-data-impl`, and `yangkit-parser`.

## Features

- Serialize YANG data to JSON
- Deserialize JSON to `YangDataDocument`
- Support container, list, leaf, leaf-list, anydata, anyxml, notification, rpc, action, and structure nodes
- Support document-level `anydata` payload schema resolution through `AnydataValidationOptions`

## Dependency

```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-json-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

## Basic Usage

### Deserialize a JSON document

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext schemaContext = ...;
JsonNode jsonNode = new ObjectMapper().readTree(jsonText);

ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
YangDataDocument document = new YangDataDocumentJsonCodec(schemaContext)
        .deserialize(jsonNode, validatorResultBuilder);
```

### Deserialize through the parser facade

```java
import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext schemaContext = ...;
JsonNode jsonNode = ...;
ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

YangDataDocument document = new YangDataDocumentJsonParser(schemaContext)
        .parse(jsonNode, validatorResultBuilder);
```

## Anydata Validation Context

JSON deserialization can resolve embedded `anydata` payloads with a document-level `AnydataValidationOptions` object.
The options are evaluated once per embedded `anydata` node during deserialization.

Shared API details are described in `../yangkit-data-api/README.md`.

### Schema-node-based matching

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext outerSchemaContext = ...;
YangSchemaContext payloadSchemaContext = ...;
JsonNode jsonNode = new ObjectMapper().readTree(
        "{" +
        "\"outer-anydata:anydata-wrapper\":{" +
        "\"payload-holder\":{" +
        "\"payload-anydata:payload-root\":{" +
        "\"value\":\"abc\"" +
        "}" +
        "}" +
        "}" +
        "}");

AnydataValidationOptions options = new AnydataValidationOptions()
        .registerSchemaContext(
                new QName("urn:test:outer-anydata", "payload-holder"),
                payloadSchemaContext);

ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
        .deserialize(jsonNode, validatorResultBuilder, options);
```

### Rule-based matching

Use `addRule(...)` when the same schema node may need different payload schema contexts depending on path or source document.

```java
AnydataValidationOptions options = new AnydataValidationOptions()
        .addRule(
                request -> request != null
                        && request.getSourcePath() != null
                        && request.getSourcePath().contains("payload-holder"),
                payloadSchemaContext);
```

### Behavior when no context matches

If no matching payload schema context is found:

- the outer `anydata` node is still created
- the embedded payload document is still created
- unrecognized payload nodes may not appear as parsed YANG data children

## Notes

- `YangDataDocumentJsonCodec.deserialize(..., options)` is the main entry when you already have a `JsonNode`
- `YangDataDocumentJsonParser.parse(..., options)` is the main entry when you prefer the parser facade
- `AnydataValidationOptions` matching order is: rule > schema-node registration > default context

## Complete Minimal Runnable Example

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangYinParser;

public class JsonAnydataExample {
    public static void main(String[] args) throws Exception {
        YangSchemaContext outerSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/outer/yang");
        YangSchemaContext payloadSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/payload/yang");

        if (!outerSchemaContext.validate().isOk() || !payloadSchemaContext.validate().isOk()) {
            throw new IllegalStateException("schema validation failed");
        }

        String json = "{" +
                "\"outer-anydata:anydata-wrapper\":{" +
                "\"payload-holder\":{" +
                "\"payload-anydata:payload-root\":{" +
                "\"value\":\"abc\"" +
                "}" +
                "}" +
                "}" +
                "}";

        JsonNode jsonNode = new ObjectMapper().readTree(json);
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(
                        new QName("urn:test:outer-anydata", "payload-holder"),
                        payloadSchemaContext);

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentJsonCodec(outerSchemaContext)
                .deserialize(jsonNode, validatorResultBuilder, options);

        YangData<?> wrapper = document.getDataChildren().get(0);
        AnyDataData anyDataData = (AnyDataData) ((YangDataContainer) wrapper).getDataChildren().get(0);

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


