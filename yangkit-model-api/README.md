# Yangkit-model-api
## overview
Yangkit-model-api provide APIs to access YANG model, it can be used to get and/edit/validate YANG statements.

## Features
* provide APIs for Yang statements.
* provide APIs for Yang restrictions(such as int8/string/leafref, etc.).
* support register yang unknown statements(defined by extension).
* provide APIs for YANG schema(e.g. schema context).
* provide codec APIs for yang data value, it can be used to encode or decode the yang data value.

## Installation
add the maven dependency to your pom.xml

        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-model-api</artifactId>
            <version>1.0.0</version>
        </dependency>
## Documentation
[Javadoc](apidocs/index.html)
