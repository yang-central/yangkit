import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.examples.App1;
import org.yangcentral.yangkit.examples.App4;
import org.yangcentral.yangkit.examples.App5;
import org.yangcentral.yangkit.examples.App6;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.parser.YangParser;
import org.yangcentral.yangkit.parser.YangParserEnv;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.register.YangStatementImplRegister;
import org.yangcentral.yangkit.register.YangStatementRegister;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IETFCasesTest {

  /**
   * Validates custom insa-test.yang
   */
  @Test
  public void validateApp1Example() throws IOException {
    InputStream inputStream = App1.class.getClassLoader().getResourceAsStream("App1/insa-test.yang");
    assertNotNull(inputStream);
    String yang = new String(IOUtils.toByteArray(inputStream));
    inputStream.close();

    YangStatementImplRegister.registerImpl();
    YangParser yangParser = new YangParser();
    YangParserEnv yangParserEnv = new YangParserEnv();
    yangParserEnv.setYangStr(yang);
    yangParserEnv.setFilename("insa-test");
    yangParserEnv.setCurPos(0);
    List<YangElement> yangElements;
    try {
      yangElements = yangParser.parseYang(yang, yangParserEnv);
    } catch (YangParserException e) {
      throw new RuntimeException(e);
    }

    assertEquals(yangElements.size(), 1);
    YangSchemaContext context = YangStatementRegister.getInstance().getSchemeContextInstance();
    // Add the yang module to the context;
    for (YangElement element : yangElements) {
      if (element instanceof YangStatement) {
        context.addModule((Module) element);
      }
    }
    ValidatorResult validatorResult = context.validate();
    assertTrue(validatorResult.isOk());
  }

  /**
   * Validates ietf-notification message (from ietf-yang-push.yang)
   */
  @Test
  @Disabled
  public void validateApp4Example() throws IOException, DocumentException, YangParserException {
    URL yangUrl = App4.class.getClassLoader().getResource("App4/yang");
    String yangDir = yangUrl.getFile();

    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();
    assertTrue(result.isOk());
    assertEquals(schemaContext.getModules().size(), 15);

    // Validating valid notification
    InputStream jsonInputStream = App4.class.getClassLoader().getResourceAsStream("App4/json/valid_notification.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(jsonElement, validatorResultBuilder);
    doc.update();
    ValidatorResult validatorResult = validatorResultBuilder.build();

    System.out.println(validatorResult.getRecords());
    assertTrue(validatorResult.isOk());
    doc.validate();
    assertTrue(validatorResult.isOk());
  }

  /**
   * Validates ietf-interfaces.yang and JSON encoded message
   */
  @Test
  public void validateApp5Example() throws IOException, DocumentException, YangParserException {
    URL yangUrl = App5.class.getClassLoader().getResource("App5/interfaces");
    String yangDir = yangUrl.getFile();

    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();
    assertTrue(result.isOk());
    assertEquals(schemaContext.getModules().size(), 3);

    InputStream jsonInputStream = App5.class.getClassLoader().getResourceAsStream("App5/json/interface.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Validating
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(jsonElement, validatorResultBuilder);
    doc.update();
    ValidatorResult validatorResult = validatorResultBuilder.build();
    assertTrue(validatorResult.isOk());
    doc.validate();
    assertTrue(validatorResult.isOk());
  }

  /**
   * Validates ietf-telemetry-message.yang and JSON encoded message
   */
  @Test
  public void validateApp6Example() throws IOException, DocumentException, YangParserException {
    URL yangUrl = App6.class.getClassLoader().getResource("App6/yangs");
    String yangDir = yangUrl.getFile();

    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();
    assertTrue(result.isOk());
    assertEquals(schemaContext.getModules().size(), 30);

    InputStream jsonInputStream = App6.class.getClassLoader().getResourceAsStream("App6/json/telemetry-msg.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Validating
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(jsonElement, validatorResultBuilder);
    doc.update();
    ValidatorResult validatorResult = validatorResultBuilder.build();
    assertTrue(validatorResult.isOk());
    doc.validate();
    assertTrue(validatorResult.isOk());
  }

}
