package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationContextResolver;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.codec.YangDataDocumentCodec;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.data.impl.util.YangDataUtil;
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
    private boolean onlyConfig; // Filter non-config data
    private AnydataValidationContextResolver anydataValidationContextResolver;

    public YangDataDocumentXmlCodec(YangSchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        this.onlyConfig = false; // Default: include all data
    }
    
    public YangDataDocumentXmlCodec(YangSchemaContext schemaContext, boolean onlyConfig) {
        this.schemaContext = schemaContext;
        this.onlyConfig = onlyConfig;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    protected ValidatorResult buildChildrenData(YangDataContainer yangDataContainer, Element element){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        SchemaNodeContainer schemaNodeContainer= null;
        if(yangDataContainer instanceof YangDataDocument){
            schemaNodeContainer = YangDataUtil.getSchemaNodeContainerForDocument((YangDataDocument) yangDataContainer);
        }else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaNodeContainer = (SchemaNodeContainer) yangData.getSchemaNode();
        }
        List<Element> children = element.elements();
        for (Element child : children) {
            SchemaNode sonSchemaNode = schemaNodeContainer.getTreeNodeChild(
                    new org.yangcentral.yangkit.common.api.QName(child.getNamespaceURI(),
                            child.getNamespacePrefix(),child.getQName().getName()));

            if (sonSchemaNode == null) {
                // Check if this is an augmented node or unknown element
                continue;
            }
            
            // Filter non-config data if onlyConfig is true
            if (onlyConfig && !isConfigTrue(sonSchemaNode)) {
                continue; // Skip non-config data
            }

            YangDataXmlCodec xmlCodec = YangDataXmlCodec.getInstance(sonSchemaNode,
                    anydataValidationContextResolver, child.getUniquePath());
            if (xmlCodec != null) {
                YangData<?> childData = xmlCodec.deserialize(child, validatorResultBuilder);
                if (childData != null) {
                    try {
                        yangDataContainer.addDataChild(childData);
                    } catch (org.yangcentral.yangkit.data.api.exception.YangDataException e) {
                        // Log error but continue processing
                        System.err.println("Warning: Failed to add child data: " + e.getMessage());
                    }
                }
            }
        }
        return validatorResultBuilder.build();
    }
    
    @Override
    public YangDataDocument deserialize(Element root, ValidatorResultBuilder validatorResultBuilder) {
        return deserialize(root, validatorResultBuilder, (AnydataValidationContextResolver) null);
    }

    @Override
    public YangDataDocument deserialize(Element root, ValidatorResultBuilder validatorResultBuilder,
                                        AnydataValidationContextResolver resolver) {
        if (null == root) {
            return null;
        }
        this.anydataValidationContextResolver = resolver;
        org.yangcentral.yangkit.common.api.QName docQName = Converter.convert(root.getQName());

        YangDataDocument yangDataDocument = new YangDataDocumentImpl(docQName, schemaContext);
        processAttributers(yangDataDocument, root);
        YangDataContainer yangDataContainer = yangDataDocument;
        validatorResultBuilder.merge(buildChildrenData(yangDataContainer,root));

        return yangDataDocument;
    }

    @Override
    public YangDataDocument deserialize(Element root, ValidatorResultBuilder validatorResultBuilder,
                                        AnydataValidationOptions options) {
        return deserialize(root, validatorResultBuilder, (AnydataValidationContextResolver) options);
    }

    public YangDataDocument deserialize(Document element, ValidatorResultBuilder resultBuilder) {
        return deserialize(element, resultBuilder, (AnydataValidationContextResolver) null);
    }

    public YangDataDocument deserialize(Document element, ValidatorResultBuilder resultBuilder,
                                        AnydataValidationContextResolver resolver) {
        if (null == element) {
            return null;
        }
        Element root = element.getRootElement();
        return deserialize(root, resultBuilder, resolver);
    }

    public YangDataDocument deserialize(Document element, ValidatorResultBuilder resultBuilder,
                                        AnydataValidationOptions options) {
        return deserialize(element, resultBuilder, (AnydataValidationContextResolver) options);
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
            
            // Filter non-config data during serialization if onlyConfig is true
            if (onlyConfig && !isConfigTrue((org.yangcentral.yangkit.model.api.stmt.SchemaNode) child.getSchemaNode())) {
                continue; // Skip non-config data
            }
            
            Element childElement = YangDataXmlCodec
                    .getInstance(child.getSchemaNode())
                    .serialize(child);
            root.add(childElement);
        }
        return root;
    }
    
    /**
     * Check if a schema node has config=true.
     * 
     * @param schemaNode the schema node to check
     * @return true if config=true or not specified (default is true), false otherwise
     */
    private boolean isConfigTrue(org.yangcentral.yangkit.model.api.stmt.SchemaNode schemaNode) {
        if (schemaNode == null) {
            return true; // Default to config
        }
        
        // Inherit from parent
        org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer parentContainer = schemaNode.getParentSchemaNode();
        if (parentContainer instanceof org.yangcentral.yangkit.model.api.stmt.SchemaNode) {
            return isConfigTrue((org.yangcentral.yangkit.model.api.stmt.SchemaNode) parentContainer);
        }
        return true; // Root defaults to config=true
    }

}

