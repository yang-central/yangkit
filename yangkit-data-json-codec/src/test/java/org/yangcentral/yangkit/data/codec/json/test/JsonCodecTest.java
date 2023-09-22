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

public class JsonCodecTest {
    @Test
    public void test_case_01() throws DocumentException, IOException, YangParserException {
        //build schema context
        URL yangUrl = this.getClass().getClassLoader().getResource("yang");
        String yangDir = yangUrl.getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult validatorResult = schemaContext.validate();
        if(!validatorResult.isOk()){
            System.out.println(validatorResult);
            return;
        }

        //load notification message
        InputStream jsonInputStream = this.getClass().getClassLoader()
                .getResourceAsStream("message/notification_message_01.json");
        Reader jsonReader = new InputStreamReader(jsonInputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonReader);
        //deserialize the json node
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodec = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage message = notificationMessageJsonCodec.deserialize(jsonNode,validatorResultBuilder);
        validatorResult = validatorResultBuilder.build();
        if(!validatorResult.isOk()){
            System.out.println(validatorResult);
            return;
        }
        validatorResult = message.validate();
        if(!validatorResult.isOk()){
            System.out.println(validatorResult);
        }
        else {
            System.out.println("deserialize success");
        }
        String str = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(notificationMessageJsonCodec.serialize(message));

        System.out.println(str);

    }
}
