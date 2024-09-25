package org.yangcentral.yangkit.data.codec.json.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class JsonCodecDataTest {

  @Test
  public void valid_data() throws DocumentException, IOException, YangParserException, URISyntaxException {
    String yangDir = this.getClass().getClassLoader().getResource("data/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 1);
    assertEquals(result.getRecords(), null);

//    String xpath = "/ity:insa-container";
//    URI uri = new URI(xpath);
//    SchemaPath.Absolute path = AbsoluteSchemaPath.from(schemaContext.)
    //TODO: get module from a yang prefix
    Module yangModule = schemaContext.getModules().get(0);
    Container container = (Container) yangModule.getDataNodeChildren().get(0);

    assertEquals(container.getArgStr(), "insa-container");

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("data/json/valid_data.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Data validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    ContainerDataJsonCodec containerDataJsonCodec = new ContainerDataJsonCodec(container);

    ContainerData containerData = containerDataJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    System.out.println(validationResult);
    assertTrue(validationResult.isOk(), "Message is valid");
  }

  @Test
  public void invalid_data_type() throws DocumentException, IOException, YangParserException, URISyntaxException {
    String yangDir = this.getClass().getClassLoader().getResource("data/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 1);
    assertEquals(result.getRecords(), null);

    Module yangModule = schemaContext.getModules().get(0);
    Container container = (Container) yangModule.getDataNodeChildren().get(0);

    assertEquals(container.getArgStr(), "insa-container");

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("data/json/invalid_data_type.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Data validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    ContainerDataJsonCodec containerDataJsonCodec = new ContainerDataJsonCodec(container);

    ContainerData containerData = containerDataJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertFalse(validationResult.isOk(), "Message should be invalid, type String in int8 leaf");
  }


  @Test
  public void invalid_data_must() throws DocumentException, IOException, YangParserException, URISyntaxException {
    String yangDir = this.getClass().getClassLoader().getResource("data/yang").getFile();
    // Parsing module
    YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
    ValidatorResult result = schemaContext.validate();

    assertTrue(result.isOk(), "All yang modules are valid YANGs");
    assertEquals(schemaContext.getModules().size(), 1);
    assertEquals(result.getRecords(), null);

    Module yangModule = schemaContext.getModules().get(0);
    Container container = (Container) yangModule.getDataNodeChildren().get(0);

    assertEquals(container.getArgStr(), "insa-container");

    // Parsing JSON
    InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("data/json/invalid_data_must.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonElement = objectMapper.readTree(jsonInputStream);

    // Data validation
    ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
    ContainerDataJsonCodec containerDataJsonCodec = new ContainerDataJsonCodec(container);

    ContainerData containerData = containerDataJsonCodec.deserialize(jsonElement, validatorResultBuilder);
    ValidatorResult validationResult = validatorResultBuilder.build();
    assertFalse(validationResult.isOk(), "Message should be invalid, mandatory leaf not present");
  }
}
