package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonCodecTest {
    @Test
    public void test_case_01() throws DocumentException, IOException, YangParserException {
        //build schema context
        URL yangUrl = this.getClass().getClassLoader().getResource("yang");
        assertNotNull(yangUrl, "Test YANG directory should be present");
        String yangDir = yangUrl.getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult validatorResult = schemaContext.validate();
        assertNotNull(validatorResult, "Schema validation result should be available");

        //load notification message
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream jsonInputStream = this.getClass().getClassLoader()
                .getResourceAsStream("message/notification_message_01.json");
        assertNotNull(jsonInputStream, "Notification JSON fixture should be present");
        try (Reader jsonReader = new InputStreamReader(jsonInputStream)) {
            JsonNode jsonNode = objectMapper.readTree(jsonReader);

            ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
            NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
            NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonNode, validatorResultBuilder);

            assertNotNull(message, "Notification message should deserialize");
            assertNotNull(message.getStructureData(), "Notification structure data should be present");
            assertNotNull(message.getBody(), "Notification body should be present");
            assertNotNull(message.getNotificationData(), "Notification payload should be present");

            validatorResult = message.validate();
            assertNotNull(validatorResult, "Notification validation result should be available");

            String str = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(notificationMessageJsonCodec.serialize(message));
            assertTrue(str.contains("ietf-notification:notification"));
            assertTrue(str.contains("event-time"));
            assertTrue(str.contains("ietf-yang-push:push-change-update"));
        }
    }
}
