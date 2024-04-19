package org.yangcentral.yangkit.data.codec.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.common.CommonUtil;

import java.io.*;


public class YangDataParserExecutorJson {
    private static Logger logger = LoggerFactory.getLogger(YangDataParserExecutorJson.class);

    public static void main(String[] args)
            throws IOException, DocumentException, YangParserException, CloneNotSupportedException {
        System.setProperty("LOGDIR", System.getProperty("user.dir"));
        String jsonFile = "/Users/caowei/Documents/javaworkplace/yangkit/test.json";
        String yangPath = "/Users/caowei/Documents/javaworkplace/yangkit/yangfiles";
        long parse_yang_begin = System.currentTimeMillis();
        // load yang files
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validatorResult = schemaContext.validate();
        long parse_yang_end = System.currentTimeMillis();
        System.out.println("load yang files:" + CommonUtil.getConsumTime(parse_yang_end - parse_yang_begin));
        System.out.println(validatorResult);
        // load fullconfig document
        long read_doc_begin = System.currentTimeMillis();
        JsonNode element = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            element = objectMapper.readTree(new File(jsonFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        long read_doc_end = System.currentTimeMillis();
        System.out.println("read doc:" + CommonUtil.getConsumTime(parse_yang_end - parse_yang_begin));
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument =
                new YangDataDocumentJsonParser(schemaContext).parse(element,validatorResultBuilder);
        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        validatorResultBuilder.merge(validatorResult);
        long after = System.currentTimeMillis();
        System.out.println("deserialize doc:" + CommonUtil.getConsumTime(after - read_doc_end));
        System.out.println(validatorResultBuilder.build());
        long bak_start = System.currentTimeMillis();
        File outFile = new File(jsonFile + "_bak");
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        YangDataDocumentJsonWriter writer = new YangDataDocumentJsonWriter(yangDataDocument, new FileOutputStream(outFile));
        writer.write();
        long bak_end = System.currentTimeMillis();
        System.out.println("bak consumed time:" + CommonUtil.getConsumTime(bak_end - bak_start));
    }
}
