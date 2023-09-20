package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class AnyxmlDataXmlCodec extends YangDataXmlCodec<Anyxml, AnyxmlData> {
    protected AnyxmlDataXmlCodec(Anyxml schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyxmlData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        return (AnyxmlData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),
                DocumentHelper.createDocument(element));
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        AnyxmlData anyxmlData = (AnyxmlData) yangData;
        Document document = anyxmlData.getValue();

        for(Element child: document.getRootElement().elements()){
            child.detach();
            element.add(child);
        }
    }
}

