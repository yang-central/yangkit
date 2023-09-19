package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class YangDataWriter {
    private YangDataDocument yangDataDocument;

    private OutputStream out;

    public YangDataWriter(YangDataDocument yangDataDocument, OutputStream out) {
        this.yangDataDocument = yangDataDocument;
        this.out = out;
        initLog4j();
    }

    private void initLog4j() {
        Properties props = new Properties();
        try {
            props.load(YangDataWriter.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write() throws IOException {
        YangDataDocumentXmlCodec codec =
                new YangDataDocumentXmlCodec(yangDataDocument.getSchemaContext());
        Element root = codec.serialize(yangDataDocument);
        Document newDoc = DocumentHelper.createDocument(root);
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(newDoc);
        writer.close();
    }
}

