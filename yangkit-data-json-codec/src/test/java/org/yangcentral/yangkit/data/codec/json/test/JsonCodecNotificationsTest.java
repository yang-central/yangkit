package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.PositionalListIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
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
  public void filteredYangPushFragmentDefersOnlyMissingConditionContext() throws Exception {
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseFilteredFixture(
          "{\"filtered-test:item\":{\"stats\":{\"value\":1,\"sample\":[\"one\",\"two\"]}}}",
            new ValidatorResultBuilder());

    assertFalse(document.validate().isOk());
    document.setPartialDataFilter("/filtered-test:item/stats");
    assertTrue(document.validate().isOk());
  }

  @Test
  public void filteredYangPushFragmentStillRequiresSelectedMandatoryData() throws Exception {
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseFilteredFixture(
          "{\"filtered-test:item\":{\"stats\":{\"sample\":[\"one\",\"two\"]}}}",
          new ValidatorResultBuilder());

    document.setPartialDataFilter("/filtered-test:item/stats");

    assertFalse(document.validate().isOk());
  }

  @Test
  public void filteredYangPushFragmentStillRejectsFalseWhenWithPresentDependency() throws Exception {
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseFilteredFixture(
          "{\"filtered-test:item\":{\"kind\":\"disabled\",\"stats\":{\"value\":1,\"sample\":[\"one\",\"two\"]}}}",
            new ValidatorResultBuilder());

    document.setPartialDataFilter("/filtered-test:item/stats");

    assertFalse(document.validate().isOk());
  }

  @Test
  public void filteredYangPushFragmentStillRejectsInvalidSelectedValues() throws Exception {
    ValidatorResultBuilder parseResult = new ValidatorResultBuilder();
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseFilteredFixture(
          "{\"filtered-test:item\":{\"stats\":{\"value\":300,\"sample\":[\"one\",\"two\"]}}}",
          parseResult);

    document.setPartialDataFilter("/filtered-test:item/stats");

    assertFalse(parseResult.build().isOk());
    assertFalse(document.validate().isOk());
  }

  @Test
  public void filteredYangPushFragmentStillEnforcesSelectedMinElements() throws Exception {
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseFilteredFixture(
            "{\"filtered-test:item\":{\"stats\":{\"value\":1,\"sample\":[\"one\"]}}}",
            new ValidatorResultBuilder());

    document.setPartialDataFilter("/filtered-test:item/stats");

    assertFalse(document.validate().isOk());
  }

  @Test
  public void notificationLeafrefToDatastoreIsNotRequiredInMessage() throws Exception {
    String module = "module ln-test { yang-version 1.1; namespace urn:ln-test; prefix ln; "
        + "container cfg { leaf id { type string; } } "
        + "notification started { leaf ref { type leafref { path \"/ln:cfg/ln:id\"; } } } "
        + "container plain { leaf id { type string; } leaf ref { type leafref { path \"../id\"; } } } }";
    org.yangcentral.yangkit.data.api.model.YangDataDocument notif =
        parseModuleDocument(module, "ln-test", "{\"ln-test:started\":{\"ref\":\"x\"}}");
    assertTrue(notif.validate().isOk(), notif.validate().toString());
    org.yangcentral.yangkit.data.api.model.YangDataDocument plain =
        parseModuleDocument(module, "ln-test", "{\"ln-test:plain\":{\"id\":\"a\",\"ref\":\"x\"}}");
    assertFalse(plain.validate().isOk());
  }

  @Test
  public void keylessStateListEntriesAreDecodedByPosition() throws Exception {
    String module = "module keyless-list-test { yang-version 1.1; namespace urn:keyless-list-test; prefix kl; "
        + "container state { config false; list foreign-clock { leaf clock-id { type string; } } } }";
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        parseModuleDocument(
            module,
            "keyless-list-test",
            "{\"keyless-list-test:state\":{\"foreign-clock\":[{\"clock-id\":\"clock-1\"},{\"clock-id\":\"clock-2\"}]}}");

    assertTrue(document.validate().isOk(), document.validate().toString());
    YangData<?> stateData = document.getDataChildren("state", "urn:keyless-list-test").get(0);
    List<YangData<?>> entries =
        ((YangDataContainer) stateData).getDataChildren("foreign-clock", "urn:keyless-list-test");

    assertEquals(2, entries.size());
    assertEquals(1, ((PositionalListIdentifier) entries.get(0).getIdentifier()).getPosition());
    assertEquals(2, ((PositionalListIdentifier) entries.get(1).getIdentifier()).getPosition());
  }

  private org.yangcentral.yangkit.data.api.model.YangDataDocument parseModuleDocument(
      String module, String name, String payload) throws Exception {
    org.yangcentral.yangkit.register.YangStatementImplRegister.registerImpl();
    YangSchemaContext context =
        org.yangcentral.yangkit.register.YangStatementRegister.getInstance().getSchemeContextInstance();
    YangYinParser.parse(
        new java.io.ByteArrayInputStream(module.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
        name + ".yang",
        context);
    assertTrue(context.validate().isOk());
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        new org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser(context)
            .parse(new ObjectMapper().readTree("{\"data\":" + payload + "}"), new ValidatorResultBuilder());
    document.update();
    return document;
  }

  private org.yangcentral.yangkit.data.api.model.YangDataDocument parseFilteredFixture(
      String payload, ValidatorResultBuilder result) throws Exception {
    String module =
        "module filtered-test { yang-version 1.1; namespace urn:filtered-test; prefix ft; "
        + "container item { leaf kind { type string; mandatory true; } leaf flag { type string; } "
        + "container stats { when \"../kind = 'enabled' and ../flag = 'yes'\"; "
            + "leaf value { type uint8; mandatory true; } "
            + "leaf-list sample { type string; min-elements 2; } } } }";
    org.yangcentral.yangkit.register.YangStatementImplRegister.registerImpl();
    YangSchemaContext context =
        org.yangcentral.yangkit.register.YangStatementRegister
            .getInstance()
            .getSchemeContextInstance();
    YangYinParser.parse(
        new java.io.ByteArrayInputStream(
            module.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
        "filtered-test.yang",
        context);
    assertTrue(context.validate().isOk());
    org.yangcentral.yangkit.data.api.model.YangDataDocument document =
        new org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser(context)
            .parse(new ObjectMapper().readTree("{\"data\":" + payload + "}"), result);
    document.update();
    return document;
  }

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
  public void invalid_structure_leaf_reportsOneError() throws DocumentException, IOException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    ObjectNode jsonElement = (ObjectNode) loadJson("notification/pushupdate/json/valid_pu_notification.json");
    ((ObjectNode) jsonElement.get("ietf-notification:notification")).put("eventTime", "not-a-date");

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec codec = new NotificationMessageJsonCodec(schemaContext);
    codec.deserialize(jsonElement, validatorResultBuilder);

    List<? extends ValidatorRecord<?, ?>> errors = getErrors(validatorResultBuilder.build());
    assertEquals(1, errors.size(), "Invalid structure leaf should be parsed once: " + errors);
  }

  @Test
  public void unknown_structure_member_reportsOneError() throws DocumentException, IOException, YangParserException {
    YangSchemaContext schemaContext = loadNotificationSchemaContext();
    ObjectNode jsonElement = (ObjectNode) loadJson("notification/pushupdate/json/valid_pu_notification.json");
    ((ObjectNode) jsonElement.get("ietf-notification:notification")).put("unexpected-member", "value");

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    NotificationMessageJsonCodec codec = new NotificationMessageJsonCodec(schemaContext);
    codec.deserialize(jsonElement, validatorResultBuilder);

    List<? extends ValidatorRecord<?, ?>> errors = getErrors(validatorResultBuilder.build());
    assertEquals(1, errors.size(), "Unknown structure member should be reported once: " + errors);
    assertEquals("unrecognized field:unexpected-member", errors.get(0).getErrorMsg().getMessage());
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
