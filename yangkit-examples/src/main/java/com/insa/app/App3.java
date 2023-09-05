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

import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Usecase on how to use yang to validate a json with an augmented YANG module
 *
 */
public class App3 {
    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        InputStream inputStream = App3.class.getClassLoader().getResourceAsStream("insa-test.yang");

        // Parsing module
        YangSchemaContext schemaContext = YangYinParser.parse(inputStream, "insa-custom", null);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        for (Module module : schemaContext.getModules()) {
            System.out.println("prefix: " + module.getSelfPrefix());
            System.out.println(module.getCurRevision().get());
        }

        // Parsing from file with a yang module dependence
        System.out.println("-------------------");
        URL dependentYang = App3.class.getClassLoader().getResource("insa-test-dependence.yang");
        schemaContext = YangYinParser.parse(dependentYang.getFile(), schemaContext);
        ValidatorResult result2 = schemaContext.validate();
        System.out.println("Second yang is valid ? " + result2.isOk());
        if (!result2.isOk()) {
            for (ValidatorRecord<?, ?> record : result2.getRecords()) {
                System.out.println("Error: " + record.getErrorMsg().getMessage());
            }
        }

        System.out.println("Total modules in context: " + schemaContext.getModules().size());

        int count = 0;
        for (Module module : schemaContext.getModules()) {
            System.out.println(count + "-> prefix: " + module.getSelfPrefix());
            System.out.println(count + "-> " + module.getCurRevision().get());
            count++;
        }
        // TODO: Implement same as App2 with a augmented JSON message
    }
}
