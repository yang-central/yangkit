# yangkit-data-xml-codec

XML codec for YANG data.

## Overview

This module provides serialization and deserialization between YANG data and XML documents.
It is suitable for RFC 7950-style YANG instance documents and is also the current reference implementation for document-level `anydata` payload schema resolution.

## Features

- Serialize YANG data to XML
- Deserialize XML to `YangDataDocument`
- Support container, list, leaf, leaf-list, anydata, anyxml, notification, rpc, and action nodes
- Support RFC 7950 section 8 related operation attributes already handled in the data layer
- Support document-level `anydata` payload schema resolution through `AnydataValidationOptions`

## Dependency

```xml
<dependency>
    <groupId>io.github.yang-central.yangkit</groupId>
    <artifactId>yangkit-data-xml-codec</artifactId>
    <version>1.6.0</version>
</dependency>
```

## Basic Usage

### Deserialize an XML document

```java
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext schemaContext = ...;
Document document = DocumentHelper.parseText(xmlText);

ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
YangDataDocument yangDataDocument = new YangDataDocumentXmlCodec(schemaContext)
        .deserialize(document, validatorResultBuilder);
```

### Deserialize from the root `Element`

```java
import org.dom4j.Element;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;

Element root = ...;
ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
YangDataDocument document = new YangDataDocumentXmlCodec(schemaContext)
        .deserialize(root, validatorResultBuilder);
```

## Anydata Validation Context

XML deserialization supports document-level `anydata` payload schema injection through `AnydataValidationOptions`.
The same options instance can cover all `anydata` nodes in the document.

Shared API details are described in `../yangkit-data-api/README.md`.

### Schema-node-based matching

```java
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

YangSchemaContext outerSchemaContext = ...;
YangSchemaContext payloadSchemaContext = ...;
Document document = DocumentHelper.parseText(
        "<anydata-wrapper xmlns=\"urn:test:outer-anydata\">"
                + "<payload-holder>"
                + "<payload-root xmlns=\"urn:test:payload-anydata\">"
                + "<value>abc</value>"
                + "</payload-root>"
                + "</payload-holder>"
                + "</anydata-wrapper>");

AnydataValidationOptions options = new AnydataValidationOptions()
        .registerSchemaContext(
                new QName("urn:test:outer-anydata", "payload-holder"),
                payloadSchemaContext);

ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
YangDataDocument yangDataDocument = new YangDataDocumentXmlCodec(outerSchemaContext)
        .deserialize(document, validatorResultBuilder, options);
```

### Rule-based matching

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

- the `anydata` node itself is still created
- the payload document is still created
- payload children that are not described by the outer schema may remain unrecognized

## Notes

- `YangDataDocumentXmlCodec.deserialize(Document, ..., options)` is the most convenient entry for whole-document parsing
- `YangDataDocumentXmlCodec.deserialize(Element, ..., options)` is useful when the XML root element has already been extracted
- `AnydataValidationOptions` matching order is: rule > schema-node registration > default context

## Complete Minimal Runnable Example

```java
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangYinParser;

public class XmlAnydataExample {
    public static void main(String[] args) throws Exception {
        YangSchemaContext outerSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/outer/yang");
        YangSchemaContext payloadSchemaContext = YangYinParser.parse("src/test/resources/anydata-validation/payload/yang");

        if (!outerSchemaContext.validate().isOk() || !payloadSchemaContext.validate().isOk()) {
            throw new IllegalStateException("schema validation failed");
        }

        Document xml = DocumentHelper.parseText(
                "<anydata-wrapper xmlns=\"urn:test:outer-anydata\">"
                        + "<payload-holder>"
                        + "<payload-root xmlns=\"urn:test:payload-anydata\">"
                        + "<value>abc</value>"
                        + "</payload-root>"
                        + "</payload-holder>"
                        + "</anydata-wrapper>");

        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(
                        new QName("urn:test:outer-anydata", "payload-holder"),
                        payloadSchemaContext);

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument document = new YangDataDocumentXmlCodec(outerSchemaContext)
                .deserialize(xml, validatorResultBuilder, options);

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


