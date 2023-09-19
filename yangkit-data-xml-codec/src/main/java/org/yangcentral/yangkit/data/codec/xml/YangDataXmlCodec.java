package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.codec.YangDataCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.utils.xml.Converter;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import java.util.List;


public abstract class YangDataXmlCodec<S extends SchemaNode, D extends YangData<S>>
        implements YangDataCodec<S, D, Element> {
    private S schemaNode;

    protected YangDataXmlCodec(S schemaNode) {
        this.schemaNode = schemaNode;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaNode.getContext().getSchemaContext();
    }
    @Override
    public S getSchemaNode() {
        return schemaNode;
    }

    public static YangDataXmlCodec<?, ?> getInstance(SchemaNode dataSchemaNode) {
        if (null == dataSchemaNode) {
            return null;
        }
        if (dataSchemaNode instanceof Container) {
            return new ContainerDataXmlCodec((Container) dataSchemaNode);
        } else if (dataSchemaNode instanceof YangList) {
            return new ListDataXmlCodec((YangList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Leaf) {
            return new LeafDataXmlCodec((Leaf) dataSchemaNode);
        } else if (dataSchemaNode instanceof LeafList) {
            return new LeafListDataXmlCodec( (LeafList) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anydata) {
            return new AnyDataDataXmlCodec( (Anydata) dataSchemaNode);
        } else if (dataSchemaNode instanceof Anyxml) {
            return new AnyxmlDataXmlCodec( (Anyxml) dataSchemaNode);
        } else if (dataSchemaNode instanceof Notification){
            return new NotificationDataXmlCodec((Notification) dataSchemaNode);
        }
        else {
            throw new IllegalArgumentException("not-support data schema type");
        }
    }

    public void processAttributers(YangData yangData, Element element) {
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




    abstract protected D buildData(Element element,ValidatorResultBuilder validatorResultBuilder);


    @Override
    public D deserialize(Element element, ValidatorResultBuilder validatorResultBuilder) {
        if (null == element) {
            return null;
        }
        if (!element.getQName()
                .equals(org.dom4j.QName.get(getSchemaNode().getIdentifier().getLocalName(),
                        Namespace.get(getSchemaNode().getIdentifier().getNamespace().toString())))) {
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(element.getUniquePath());
            recordBuilder.setBadElement(element);
            recordBuilder
                    .setErrorMessage(new ErrorMessage("incompatible element, schemaNode:"
                            + getSchemaNode().getIdentifier().getQualifiedName()));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return null;
        }
        D data = buildData(element,validatorResultBuilder);
        processAttributers(data,element);
        // if(data instanceof YangDataContainer){
        //     YangDataContainer yangDataContainer = (YangDataContainer) data;
        //     ValidatorResult childrenResult = buildChildrenData(yangDataContainer,element);
        //     validatorResultBuilder.merge(childrenResult);
        // }
        return data;
    }
    abstract protected void buildElement(Element element,YangData<?> yangData);
    protected void serializeChildren(Element element,YangDataContainer yangDataContainer){
        SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) (((YangData)yangDataContainer).getSchemaNode());
        List<SchemaNode> schemaChildren = schemaNodeContainer.getTreeNodeChildren();
        if(schemaChildren.isEmpty()){
            return;
        }
        for(SchemaNode dataChild:schemaChildren){
            List<YangData<?>> childData= yangDataContainer
                    .getDataChildren(dataChild.getIdentifier());
            for(YangData<?> childDatum:childData){
                if(childDatum.isDummyNode()){
                    continue;
                }
                YangDataXmlCodec xmlCodec = getInstance(childDatum.getSchemaNode());
                Element childElement = xmlCodec.serialize(childDatum);
                element.add(childElement);
            }
        }
    }
    @Override
    public Element serialize(YangData<?> yangData) {
        Element element = DocumentHelper.createElement(Converter.convert2Dom4jQName(yangData.getQName()));
        if (null != yangData.getAttributes()) {
            element.setAttributes(Converter.convert2Dom4jAttr(element, yangData.getAttributes()));
        }
        buildElement(element,yangData);
        if(yangData instanceof YangDataContainer){
            serializeChildren(element, (YangDataContainer) yangData);
        }

        return element;
    }

}

