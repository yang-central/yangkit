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
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Usecase on how to use yang to validate YANG-push notification
 *
 */
public class App5 {
    public static void main(String[] args) throws IOException, YangParserException, DocumentException {

        URL yangUrl = App5.class.getClassLoader().getResource("App5/interfaces");
        String yangDir = yangUrl.getFile();

        // Parsing module
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        int count = 0;
        for (Module module : schemaContext.getModules()) {
            System.out.println(count + "->Module["+ module.getModuleId().getModuleName()+"] prefix[" + module.getSelfPrefix()+"] | revision[" + module.getCurRevision().get() + "] ; root? [" + module.isSchemaTreeRoot() + "]");
            count++;
        }

        if (!result.isOk()) {
            for (ValidatorRecord<?, ?> record : result.getRecords()) {
                System.out.println("Error: "+ record.getBadElement() + ':' + record.getErrorMsg().getMessage());
            }
        } else {
            System.out.println("YANGs valid");
            for (ValidatorRecord<?, ?> record : result.getRecords()) {
                System.out.println("valid: "+ record.getBadElement() + ':' + record.getErrorMsg().getMessage() + "; Severity:" + record.getSeverity().getFieldName());
            }
        }
        System.out.println("------------ Validating valid message ------------");
        InputStream jsonInputStream = App5.class.getClassLoader().getResourceAsStream("App5/json/interface.json");
        JsonNode jsonElement = null;
        ObjectMapper objectMapper = new ObjectMapper();
        jsonElement = objectMapper.readTree(jsonInputStream);

        System.out.println("Valid Message: " + jsonElement);

        // Validating
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(jsonElement, validatorResultBuilder);
        doc.update();
        ValidatorResult validatorResult = validatorResultBuilder.build();

        System.out.println("Is deserialization ok (should be true)? " + validatorResult.isOk());

        ValidatorResult validationResult = doc.validate();
        System.out.println("second validation: " + validationResult.isOk());
        if (!validationResult.isOk()) System.out.println(validationResult);

    }
}
