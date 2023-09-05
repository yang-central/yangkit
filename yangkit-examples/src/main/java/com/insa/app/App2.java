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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Usecase on how to use yang to validate a json
 *
 */
public class App2 {
    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        InputStream inputStream = App2.class.getClassLoader().getResourceAsStream("insa-test.yang");

        // Parsing module
        YangSchemaContext schemaContext = YangYinParser.parse(inputStream, "insa-custom", null);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        int count = 0;
        for (Module module : schemaContext.getModules()) {
            System.out.println(count + "-> prefix: " + module.getSelfPrefix());
            System.out.println(count + "-> " + module.getCurRevision().get());
            count++;
        }

        if (!result.isOk()) {
            for (ValidatorRecord<?, ?> record : result.getRecords()) {
                System.out.println("Error: " + record.getErrorMsg().getMessage());
            }
        }

        // Get JSON message
        InputStream jsonInputStream = App2.class.getClassLoader().getResourceAsStream("json/insa-test.json");
        Reader jsonReader = new InputStreamReader(jsonInputStream);
        JsonElement jsonElement = new JsonParser().parse(jsonReader).getAsJsonObject();
        System.out.println(jsonElement);

        // Validating
        YangDataDocumentJsonCodec yangDataDocumentJsonCodec = new YangDataDocumentJsonCodec();
        YangDataDocument document = yangDataDocumentJsonCodec.deserialize(jsonElement, new ValidatorResultBuilder());
        ValidatorResult validatorResult = document.validate();

        if (validatorResult.isOk()) {
            System.out.println("JSON is valid");
        } else {
            for (ValidatorRecord<?, ?> record : validatorResult.getRecords()) {
                System.out.println("Error: " + record.getErrorMsg().getMessage());
            }
        }
    }
}
