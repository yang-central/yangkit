package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Disabled;
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

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class JsonCodecNotificationsTest {

  @Test
  public void valid_pushUpdate_notification() throws DocumentException, IOException, YangParserException {
    String yangDir = this.getClass().getClassLoader().getResource("notification/pushupdate/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 15);
    for (ValidatorRecord<?, ?> record : result.getRecords()) {
      assertNotEquals(record.getSeverity(), Severity.ERROR);
    }

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("notification/pushupdate/json/valid_pu_notification.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Message validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertTrue(validationResult.isOk(), "Message is valid");

    ValidatorResult messageValidation = message.validate();
    assertTrue(messageValidation.isOk());
  }

  @Test
  public void valid_pushChangeUpdate_notification() throws DocumentException, IOException, YangParserException {
    String yangDir = this.getClass().getClassLoader().getResource("notification/pushupdate/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 15);
    for (ValidatorRecord<?, ?> record : result.getRecords()) {
      assertNotEquals(record.getSeverity(), Severity.ERROR);
    }

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("notification/pushupdate/json/valid_pcu_notification.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Message validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertTrue(validationResult.isOk(), "Message is valid");

    ValidatorResult messageValidation = message.validate();
    assertTrue(messageValidation.isOk());
  }
  @Test
  public void invalid_notification_header() throws DocumentException, IOException, YangParserException {
    String yangDir = this.getClass().getClassLoader().getResource("notification/pushupdate/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 15);
    for (ValidatorRecord<?, ?> record : result.getRecords()) {
      assertNotEquals(record.getSeverity(), Severity.ERROR);
    }

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("notification/pushupdate/json/invalid_header_pu_notification.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Message validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertTrue(!validationResult.isOk(), "eventTime leaf is not present");

    ValidatorResult messageValidation = message.validate();
    assertTrue(!messageValidation.isOk());
  }

  @Test
  public void invalid_notification_payload_must() throws IOException, DocumentException, YangParserException {
    String yangDir = this.getClass().getClassLoader().getResource("notification/pushupdate/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 15);
    for (ValidatorRecord<?, ?> record : result.getRecords()) {
      assertNotEquals(record.getSeverity(), Severity.ERROR);
    }

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("notification/pushupdate/json/invalid_payload_pu_notification_1.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Message validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertTrue(!validationResult.isOk(), "Message is not valid");

    ValidatorResult messageValidation = message.validate();
    assertTrue(!messageValidation.isOk());
  }

  @Test
  public void invalid_notification_payload_type() throws IOException, DocumentException, YangParserException {
    String yangDir = this.getClass().getClassLoader().getResource("notification/pushupdate/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 15);
    for (ValidatorRecord<?, ?> record : result.getRecords()) {
      assertNotEquals(record.getSeverity(), Severity.ERROR);
    }

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("notification/pushupdate/json/invalid_payload_pu_notification_2.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Message validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
    NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertTrue(!validationResult.isOk(), "Message is not valid");

    ValidatorResult messageValidation = message.validate();
    assertTrue(!messageValidation.isOk());
  }
}
