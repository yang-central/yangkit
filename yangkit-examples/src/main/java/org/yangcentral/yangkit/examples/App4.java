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

package org.yangcentral.yangkit.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
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
public class App4 {
    public static void main(String[] args) throws IOException, YangParserException, DocumentException {

//        InputStream inputStream = App4.class.getClassLoader().getResourceAsStream("App4/yang/insa-test.yang");
        //TODO: should change to inputStream (accomodate to schema registry)?
        URL yangUrl = App4.class.getClassLoader().getResource("App4/yang");
        String yangDir = yangUrl.getFile();

        // Parsing module
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        // TODO: the Xpath is needed?
//        Container subscribedContainer = null;

        int count = 0;
        for (Module module : schemaContext.getModules()) {
            System.out.println(count + "->Module["+ module.getModuleId().getModuleName()+"] prefix[" + module.getSelfPrefix()+"] | revision[" + module.getCurRevision().get() + "]");
            count++;
        }

        if (!result.isOk()) {
            for (ValidatorRecord<?, ?> record : result.getRecords()) {
                System.out.println("Error: " + record.getErrorMsg().getMessage());
            }
        }
        System.out.println("------------ Validating valid message ------------");
        InputStream jsonInputStream = App4.class.getClassLoader().getResourceAsStream("App4/json/valid_notification.json");
        JsonNode jsonElement = null;
        ObjectMapper objectMapper = new ObjectMapper();
        jsonElement = objectMapper.readTree(jsonInputStream);

        System.out.println("Valid Message: " + jsonElement);

        // Validating
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
        ValidatorResult validationResult = validatorResultBuilder.build();
        System.out.println("Is deserialization ok (should be true)? " + validationResult.isOk());
        if (!validationResult.isOk()) System.out.println(validationResult);

        validationResult = message.validate();
        System.out.println("Is deserialized data valid (should be true)? " + validationResult.isOk());
        if (!validationResult.isOk()) System.out.println(validationResult);

        NotificationData notificationData = message.getNotificationData();
        ValidatorResult notifDataValid = notificationData.validate();

        System.out.println("Is notification data valid (should be true)? " + notifDataValid.isOk());
        if (!notifDataValid.isOk()) System.out.println(notifDataValid);

        System.out.println("------------ Validating invalid message ------------");
        jsonInputStream = App4.class.getClassLoader().getResourceAsStream("App4/json/invalid_notification.json");
        objectMapper = new ObjectMapper();
        jsonElement = objectMapper.readTree(jsonInputStream);

        System.out.println("Valid Message: " + jsonElement);

        // Validating
        validatorResultBuilder = new ValidatorResultBuilder();
        notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
        message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
        validationResult = validatorResultBuilder.build();
        System.out.println("Is deserialization ok (should be false)? " + validationResult.isOk());
        if (!validationResult.isOk()) System.out.println(validationResult);

        validationResult = message.validate();
        System.out.println("Is deserialized data valid (should be false)? " + validationResult.isOk());
        if (!validationResult.isOk()) System.out.println(validationResult);

        notificationData = message.getNotificationData();
        notifDataValid = notificationData.validate();

        System.out.println("Is notification data valid (should be false)? " + notifDataValid.isOk());
        if (!notifDataValid.isOk()) System.out.println(notifDataValid);
    }
}
