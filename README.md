# Yangkit

## overview

Yangkit is a toolkit for YANG([RFC7950](https://datatracker.ietf.org/doc/html/rfc7950)) data model language including YANG parser,YANG data and other tools.

## Yangkit components
* [yangkit-parser](yangkit-parser/README.md): parse YANG files and validate the parsed YANG modules
* [yangkit-model-api](yangkit-model-api/README.md): The APIs for YANG model
* [yangkit-model-impl](yangkit-model-impl/README.md): The implementations for YANG model
* [yangkit-xpath-api](yangkit-xpath-api/README.md): The APIs for YANG XPATH parser,validator and evaluator.
* [yangkit-xpath-impl](yangkit-xpath-impl/README.md): The Implementations for YANG XPATH parser,validator and evaluator.
* [yangkit-data-api](yangkit-data-api/README.md): The APIs for YANG data representation and operation.
* [yangkit-data-impl](yangkit-data-impl/README.md): The Implementations for YANG data representation and operation.

## Installation
### From source
git clone https://github.com/yang-central/yangkit.git

and execute the maven command:

mvn clean install

### maven dependency
#### yangkit-parser
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-parser</artifactId>
            <version>1.0.0</version>
        </dependency>
#### yangkit-model-api
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-model-api</artifactId>
            <version>1.0.0</version>
        </dependency>
#### yangkit-model-impl
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-model-impl</artifactId>
            <version>1.0.0</version>
        </dependency>
#### yangkit-xpath-api
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-xpath-api</artifactId>
            <version>1.0.0</version>
        </dependency>
#### yangkit-xpath-impl
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-xpath-impl</artifactId>
            <version>1.0.0</version>
        </dependency>
#### yangkit-data-api
        <dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-data-api</artifactId>
            <version>1.0.0</version>
        </dependency>
## Documentation
Please see the java doc in components.