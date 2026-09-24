package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationSupport;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;
import org.dom4j.Element;

public class AnyDataDataXmlCodec extends YangDataXmlCodec<Anydata, AnyDataData> {

    protected AnyDataDataXmlCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected AnyDataData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        if (element.elements().isEmpty()) {
            if (element.getTextTrim() != null && !element.getTextTrim().isEmpty()) {
                AnydataValidationSupport.recordInvalidContent(
                        getSchemaNode(), getSourcePath(), element,
                        "XML content must contain child elements.", validatorResultBuilder);
            }
            return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        }
        YangSchemaContext payloadSchemaContext = AnydataValidationSupport.resolveSchemaContext(
                getSchemaNode(), getSourcePath(), getSchemaContext(),
                getAnydataValidationContextResolver(), element, validatorResultBuilder);
        if (payloadSchemaContext == null) {
            return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        }
        YangDataDocumentXmlCodec documentXmlCodec = new YangDataDocumentXmlCodec(payloadSchemaContext);
        YangDataDocument dataDocument = documentXmlCodec.deserialize(element, validatorResultBuilder,
                getAnydataValidationContextResolver());

        return (AnyDataData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), dataDocument);
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        AnyDataData anyDataData = (AnyDataData) yangData;
        YangDataDocument document = anyDataData.getEffectiveValue();
        if (document == null) {
            return;
        }

        YangDataDocumentXmlCodec documentXmlCodec = new YangDataDocumentXmlCodec(document.getSchemaContext());
        Element root = documentXmlCodec.serialize(document);
        for (Element child : root.elements()) {
            child.detach();
            element.add(child);
        }
    }
}
