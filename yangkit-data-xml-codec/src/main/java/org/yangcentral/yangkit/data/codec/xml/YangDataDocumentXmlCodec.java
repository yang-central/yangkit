package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataDocumentCodec;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.utils.xml.Converter;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.util.List;

public class YangDataDocumentXmlCodec implements YangDataDocumentCodec<Element> {
    private YangSchemaContext schemaContext;

    public YangDataDocumentXmlCodec(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    protected ValidatorResult buildChildrenData(YangDataContainer yangDataContainer,Element element){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        SchemaNodeContainer schemaNodeContainer= null;
        if(yangDataContainer instanceof YangDataDocument){
            schemaNodeContainer = ((YangDataDocument) yangDataContainer).getSchemaContext();
        }else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaNodeContainer = (SchemaNodeContainer) yangData.getSchemaNode();
        }
        List<Element> children = element.elements();
        for (Element child : children) {
            SchemaNode sonSchemaNode = schemaNodeContainer.getTreeNodeChild(
                    new org.yangcentral.yangkit.common.api.QName(child.getNamespaceURI(),
                            child.getNamespacePrefix(),child.getQName().getName()));

            if (sonSchemaNode == null || !sonSchemaNode.isActive()) {
                ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.UNKNOWN_ELEMENT);
                recordBuilder.setErrorPath(element.getUniquePath());
                recordBuilder.setBadElement(child);
                recordBuilder.setErrorMessage(new ErrorMessage(
                        "unrecognized element:" + child.getName() + " namespace:" + child.getQName().getNamespaceURI()));
                validatorResultBuilder.addRecord(recordBuilder.build());
                continue;
            }
            YangDataXmlCodec sonCodec = YangDataXmlCodec.getInstance(sonSchemaNode);
            YangData<?> sonData = sonCodec.deserialize(child,validatorResultBuilder);
            if(null== sonData){
                continue;
            }
            try {
                YangData<?> oldData = yangDataContainer.getDataChild(sonData.getIdentifier());
                if(oldData != null){
                    YangDataOperator dataOperator = new YangDataOperatorImpl(yangDataContainer);
                    dataOperator.merge((YangData<? extends DataNode>) sonData,false);
                    //oldData.merge(sonData,false);
                    //sonData = oldData;
                }
                else {
                    yangDataContainer.addDataChild(sonData,false);
                }

            } catch (YangDataException e) {
                ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(e.getErrorTag());
                recordBuilder.setErrorPath(child.getUniquePath());
                recordBuilder.setBadElement(child);
                recordBuilder.setErrorMessage(e.getErrorMsg());
                validatorResultBuilder.addRecord(recordBuilder.build());
                continue;
            }

            sonData = yangDataContainer.getDataChild(sonData.getIdentifier());
            if(sonData instanceof YangDataContainer){
                validatorResultBuilder.merge(buildChildrenData((YangDataContainer) sonData,child));
            }
        }
        return validatorResultBuilder.build();

    }
    @Override
    public YangDataDocument deserialize(Element root, ValidatorResultBuilder validatorResultBuilder) {
        if (null == root) {
            return null;
        }
        org.yangcentral.yangkit.common.api.QName docQName = Converter.convert(root.getQName());

        YangDataDocument yangDataDocument = new YangDataDocumentImpl(docQName, schemaContext, root.toString());
        processAttributers(yangDataDocument, root);
        YangDataContainer yangDataContainer = yangDataDocument;
        validatorResultBuilder.merge(buildChildrenData(yangDataContainer,root));

        return yangDataDocument;
    }

    public YangDataDocument deserialize(Document element, ValidatorResultBuilder resultBuilder) {
        if (null == element) {
            return null;
        }
        Element root = element.getRootElement();
        return deserialize(root, resultBuilder);
    }



    public void processAttributers(YangDataDocument yangData, Element element) {
        if (null == element) {
            return;
        }
        if (null != element.attributes()) {
            for (Attribute attribute : element.attributes()) {
                if (null == attribute) {
                    continue;
                }
                yangData.addAttribute(Converter.convert(attribute));
            }
        }

    }



    private void buildAttributes(Element element, List<org.yangcentral.yangkit.common.api.Attribute> attributes) {
        if (null == attributes) {
            return;
        }
        List<Attribute> dom4jAttributes = Converter.convert2Dom4jAttr(element, attributes);
        element.setAttributes(dom4jAttributes);
    }

    @Override
    public Element serialize(YangDataDocument yangDataDocument) {
        if (null == yangDataDocument) {
            return null;
        }

        Element root = DocumentHelper.createElement(Converter.convert2Dom4jQName(yangDataDocument.getQName()));
        buildAttributes(root, yangDataDocument.getAttributes());
        // Document document = DocumentHelper.createDocument(root);

        List<YangData<?>> children = yangDataDocument.getDataChildren();
        if (null == children) {
            return root;
        }
        for (YangData child : children) {
            if (null == child || child.isDummyNode()) {
                continue;
            }
            Element childElement = YangDataXmlCodec
                    .getInstance(child.getSchemaNode())
                    .serialize(child);
            root.add(childElement);
        }
        return root;
    }

}

