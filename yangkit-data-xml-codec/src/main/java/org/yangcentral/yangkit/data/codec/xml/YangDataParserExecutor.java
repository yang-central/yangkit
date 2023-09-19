package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.common.CommonUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class YangDataParserExecutor {
    private static Logger logger = LoggerFactory.getLogger(YangDataParserExecutor.class);

    public static void main(String[] args)
            throws IOException, DocumentException, YangParserException, CloneNotSupportedException {
        System.setProperty("LOGDIR", System.getProperty("user.dir"));


        String xmlPath = "/Users/caowei/Documents/javaworkplace/yangkit/test.xml";
        String yangPath = "/Users/caowei/Documents/javaworkplace/yangkit/yangfiles";

        long parse_yang_begin = System.currentTimeMillis();
        // load yang files
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validatorResult = schemaContext.validate();
        long parse_yang_end  = System.currentTimeMillis();
        System.out.println("load yang files:" + CommonUtil.getConsumTime(parse_yang_end - parse_yang_begin));
        System.out.println(validatorResult);
        // load fullconfig document
        long read_doc_begin = System.currentTimeMillis();
        SAXReader reader = new SAXReader();
        Document document;
        File xmlFile = new File(xmlPath);
        document = reader.read(xmlFile);
        long read_doc_end = System.currentTimeMillis();
        System.out.println("read doc:" + CommonUtil.getConsumTime(parse_yang_end - parse_yang_begin));
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument =
                new YangDataParser(document, schemaContext, true).parse(validatorResultBuilder);
        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        validatorResultBuilder.merge(validatorResult);
        long after = System.currentTimeMillis();
        System.out.println("deserialize doc:" + CommonUtil.getConsumTime(after - read_doc_end));
        System.out.println(validatorResultBuilder.build());
        long bak_start = System.currentTimeMillis();
        File outFile = new File(xmlPath + "_bak");
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        YangDataWriter xmlwriter = new YangDataWriter(yangDataDocument,new FileOutputStream(outFile));
        xmlwriter.write();
        long bak_end = System.currentTimeMillis();
        System.out.println("bak consumed time:" + CommonUtil.getConsumTime(bak_end - bak_start));
        // long clone_start = System.currentTimeMillis();
        // File cloneFile = new File(xmlPath + "_clone");
        // if(!cloneFile.exists()){
        //     cloneFile.createNewFile();
        // }
        // writer = new YangDataWriter(yangDataDocument.clone(),new FileOutputStream(cloneFile));
        // writer.write();
        // long clone_end = System.currentTimeMillis();
        // System.out.println("clone consumed time:" + CommonUtil.getConsumTime(clone_end - clone_start));
    }
}

