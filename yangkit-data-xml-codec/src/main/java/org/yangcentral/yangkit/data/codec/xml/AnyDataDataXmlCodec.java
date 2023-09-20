package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.Anydata;
import org.dom4j.Element;

public class AnyDataDataXmlCodec extends YangDataXmlCodec<Anydata, AnyDataData> {

    protected AnyDataDataXmlCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyDataData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        YangDataDocumentXmlCodec documentXmlCodec = new YangDataDocumentXmlCodec(getSchemaContext());
        YangDataDocument dataDocument = documentXmlCodec.deserialize(element,validatorResultBuilder);
        YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),dataDocument);
        return null;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        AnyDataData anyDataData = (AnyDataData) yangData;
        YangDataDocument document = anyDataData.getValue();
        YangDataDocumentXmlCodec documentXmlCodec = new YangDataDocumentXmlCodec(getSchemaContext());
        Element root = documentXmlCodec.serialize(document);
        for(Element child: root.elements()){
            child.detach();
            element.add(child);
        }
    }
}

