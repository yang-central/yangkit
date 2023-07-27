# Yangkit-parser
## overview
Yangkit-parser can be used to parse YANG files and generate YANG files from YANG model .

## Features
* parse single YANG file.
* parse directory contains YANG files.
* parse single YIN file.
* parse directory contains YIN files.
* parse YANG/YIN input stream.
* generate YANG files from YANG models.
* generate YIN files from YANG models.


## Installation
add the maven dependency to your pom.xml
```
<dependency>
            <groupId>io.github.yang-central.yangkit</groupId>
            <artifactId>yangkit-parser</artifactId>
            <version>1.0.0</version>
        </dependency>
```
        
## How to use
### parse yang files from directory
```
YangSchemaContext schemaContext = YangYinParser.parse(yang_dir);
ValidatorResult result = schemaContext.validate();
//do anything using schemaContext
...
```
### parse yang from input stream
```
YangSchemaContext schemaContext = YangYinParser.parse(inputstream1, "example1.yang",null);
schemaContext = YangYinParser.parse(inputstream2,"example2.yang",schemaContext);
ValidatorResult result = schemaContext.validate();
//do anything using schemaContext
```
## Documentation
[Javadoc](apidocs/index.html)