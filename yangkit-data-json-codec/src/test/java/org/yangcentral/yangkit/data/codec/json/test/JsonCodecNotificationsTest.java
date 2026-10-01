package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class JsonCodecNotificationsTest {

  @Test
  public void valid_pushUpdate_notification() throws DocumentException, IOException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    JsonNode jsonElement = loadJson("notification/pushupdate/json/valid_pu_notification.json");

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertNotNull(message, "Valid push-update notification should deserialize");
    assertNotNull(message.getStructureData(), "Valid push-update should include notification structure data");
    assertNotNull(message.getBody(), "Valid push-update should include a notification body");
    assertNotNull(message.getNotificationData(), "Valid push-update should expose notification payload data");
    assertNoErrors(validationResult, "Valid push-update parse result");

    String serialized = new ObjectMapper().writeValueAsString(notificationMessageJsonCodec.serialize(message));
    assertTrue(serialized.contains("ietf-notification:notification"));
    assertTrue(serialized.contains("eventTime"));
    assertTrue(serialized.contains("ietf-yang-push:push-update"));
  }

  @Test
  public void valid_pushChangeUpdate_notification() throws DocumentException, IOException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    JsonNode jsonElement = loadJson("notification/pushupdate/json/valid_pcu_notification.json");

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertNotNull(message, "Valid push-change-update notification should deserialize");
    assertNotNull(message.getStructureData(), "Valid push-change-update should include notification structure data");
    assertNotNull(message.getBody(), "Valid push-change-update should include a notification body");
    assertNotNull(message.getNotificationData(), "Valid push-change-update should expose notification payload data");
    assertNoErrors(validationResult, "Valid push-change-update parse result");

    String serialized = new ObjectMapper().writeValueAsString(notificationMessageJsonCodec.serialize(message));
    assertTrue(serialized.contains("ietf-notification:notification"));
    assertTrue(serialized.contains("eventTime"));
    assertTrue(serialized.contains("ietf-yang-push:push-change-update"));
  }

  @Test
  public void invalid_notification_header() throws DocumentException, IOException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    JsonNode jsonElement = loadJson("notification/pushupdate/json/invalid_header_pu_notification.json");

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult parseResult = validatorResultBuilder.build();
    boolean invalidDetected = hasErrors(parseResult);
    if (!invalidDetected && message != null) {
      invalidDetected = hasErrors(message.validate());
    }
    assertTrue(invalidDetected,
            "Missing eventTime leaf should be rejected during parse or message validation");
  }

  @Test
  public void invalid_notification_payload_must() throws IOException, DocumentException, YangParserException {
    assertInvalidNotification("notification/pushupdate/json/invalid_payload_pu_notification_1.json",
            "Missing mandatory notification payload content should be rejected");
  }

  @Test
  public void invalid_notification_payload_type() throws IOException, DocumentException, YangParserException {
    assertInvalidNotification("notification/pushupdate/json/invalid_payload_pu_notification_2.json",
            "Invalid notification payload type should be rejected");
  }

  private YangSchemaContext loadNotificationSchemaContext() throws DocumentException, IOException, YangParserException {
    URL yangDirUrl = this.getClass().getClassLoader().getResource("notification/pushupdate/yang");
    assertNotNull(yangDirUrl, "Notification YANG test directory should be present");
    String yangDir = yangDirUrl.getFile();
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();
    assertEquals(15, schemaContext.getModules().size(), "Notification test schema set should remain stable");
    assertOnlyKnownNotificationSchemaErrors(result);
    return schemaContext;
  }

  private JsonNode loadJson(String resourcePath) throws IOException {
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
    assertNotNull(jsonInputStream, "Notification JSON fixture should be present: " + resourcePath);
    return new ObjectMapper().readTree(jsonInputStream);
  }

  private void assertInvalidNotification(String jsonResource, String message)
          throws IOException, DocumentException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    JsonNode jsonElement = loadJson(jsonResource);
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);

    boolean invalidDetected = false;
    try {
      NotificationMessage notificationMessage = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
      ValidatorResult parseResult = validatorResultBuilder.build();
      invalidDetected = hasErrors(parseResult);
      if (!invalidDetected && notificationMessage != null) {
        invalidDetected = hasErrors(notificationMessage.validate());
      }
    } catch (RuntimeException e) {
      invalidDetected = true;
    }

    assertTrue(invalidDetected, message);
  }

  private void assertNoErrors(ValidatorResult result, String context) {
    List<? extends ValidatorRecord<?, ?>> errors = getErrors(result);
    if (!errors.isEmpty()) {
      fail(context + " - unexpected errors: " + errors);
    }
  }

  private void assertOnlyKnownNotificationSchemaErrors(ValidatorResult result) {
    List<? extends ValidatorRecord<?, ?>> errors = getErrors(result);
    if (errors.isEmpty()) {
      return;
    }

    List<? extends ValidatorRecord<?, ?>> unexpectedErrors = errors.stream()
            .filter(record -> record.getErrorMsg() == null
                    || !"augment mandatory node".equals(record.getErrorMsg().getMessage())
                    || record.getErrorPath() == null
                    || !record.getErrorPath().toString().contains("ietf-subscribed-notifications@2019-09-09 [augment]target/stream/[leaf]stream"))
            .collect(Collectors.toList());

    if (!unexpectedErrors.isEmpty()) {
      fail("Notification push-update schema validation produced unexpected errors: " + unexpectedErrors);
    }
  }

  private void assertHasErrors(ValidatorResult result, String context) {
    assertTrue(hasErrors(result), context + "; actual result=" + result);
  }

  private boolean hasErrors(ValidatorResult result) {
    return !getErrors(result).isEmpty();
  }

  private List<? extends ValidatorRecord<?, ?>> getErrors(ValidatorResult result) {
    if (result == null || result.getRecords() == null) {
      return new java.util.ArrayList<>();
    }
    return result.getRecords().stream()
            .filter(record -> record.getSeverity() == Severity.ERROR)
            .collect(Collectors.toList());
  }
}
