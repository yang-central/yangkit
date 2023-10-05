/*
 * Copyright 2023 INSA Lyon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.insa.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.*;

/**
 * Usecase on how to use yang to validate a json
 *
 */
public class App2 {
    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        InputStream inputStream = App2.class.getClassLoader().getResourceAsStream("App2/yang/insa-test.yang");

        // Parsing module
        YangSchemaContext schemaContext = YangYinParser.parse(inputStream, "insa-custom", null);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        // TODO: the Xpath is needed?
        Container subscribedContainer = null;

        int count = 0;
        for (Module module : schemaContext.getModules()) {
            System.out.println(count + "-> prefix: " + module.getSelfPrefix());
            System.out.println(count + "-> " + module.getCurRevision().get());
            for (DataNode dataNodeChild : module.getDataNodeChildren()) {
                Container container = (Container) dataNodeChild;
                System.out.println(container);
                subscribedContainer = container;
                System.out.println(container.getIdentifier().getQualifiedName());
                SchemaPath.Absolute schemaPath = container.getSchemaPath();
                System.out.println(schemaPath);
                for (DataNode nodeChild : container.getDataNodeChildren()) {
                    System.out.println(nodeChild);
                    Leaf leaf = (Leaf) nodeChild;
                    System.out.println(leaf.getIdentifier().getQualifiedName());
                }
            }

            count++;
        }

        if (!result.isOk()) {
            for (ValidatorRecord<?, ?> record : result.getRecords()) {
                System.out.println("Error: " + record.getErrorMsg().getMessage());
            }
        }
        System.out.println("------------ Validating valid message ------------");
        InputStream jsonInputStream = App2.class.getClassLoader().getResourceAsStream("App2/json/valid.json");
        JsonNode jsonElement = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonElement = objectMapper.readTree(jsonInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Valid Message: " + jsonElement);

        // Validating
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        ContainerDataJsonCodec containerDataJsonCodec = new ContainerDataJsonCodec(subscribedContainer);
        ContainerData deserialized = containerDataJsonCodec.deserialize(jsonElement, validatorResultBuilder);
        ValidatorResult validationResult = validatorResultBuilder.build();
        System.out.println("Is deserialization ok? " + validationResult.isOk());

        validationResult = deserialized.validate();
        System.out.println("-->" + deserialized.getIdentifier());
        System.out.println("Is deserialized data valid (should be true)? " + validationResult.isOk());

        System.out.println("------------ Validating invalid message ------------");
        InputStream jsonInputStream2 = App2.class.getClassLoader().getResourceAsStream("App2/json/invalid.json");
        ObjectMapper objectMapper2 = new ObjectMapper();
        jsonElement = objectMapper2.readTree(jsonInputStream2);

        System.out.println("Invalid Message: " + jsonElement);

        // Validating
        ValidatorResultBuilder validatorResultBuilder2 = new ValidatorResultBuilder();
        ContainerDataJsonCodec containerDataJsonCodec2 = new ContainerDataJsonCodec(subscribedContainer);
        ContainerData validDeserialized = containerDataJsonCodec.deserialize(jsonElement, validatorResultBuilder2);
        ValidatorResult validationResult2 = validatorResultBuilder2.build();
        System.out.println("Is deserialization ok? " + validationResult2.isOk());

        validationResult2 = validDeserialized.validate();
        System.out.println("-->" + deserialized.getIdentifier());
        System.out.println("Is deserialized data valid (should be false)? " + validationResult2.isOk());
//        System.out.println("-------------------- Other test --------------------");
//        InputStream jsonInputStream3 = App2.class.getClassLoader().getResourceAsStream("App2/json/valid.json");
//        ObjectMapper objectMapper3 = new ObjectMapper();
//        jsonElement = objectMapper3.readTree(jsonInputStream3);
//        ValidatorResultBuilder validatorResultBuilder3 = new ValidatorResultBuilder();
//        YangDataDocument yangDataDocument = new YangDataParser(jsonElement, schemaContext, false).parse(validatorResultBuilder3);
//        yangDataDocument.update();
//        ValidatorResult validationResult3 = validatorResultBuilder3.build();
//        System.out.println("I---> " + validationResult3.isOk());

    }
}
