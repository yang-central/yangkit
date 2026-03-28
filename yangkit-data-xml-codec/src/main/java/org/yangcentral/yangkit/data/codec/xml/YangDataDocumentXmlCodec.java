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
    private boolean onlyConfig; // Filter non-config data

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
        System.out.println("[DEBUG] buildChildrenData started for element: " + element.getQName().getName());
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        SchemaNodeContainer schemaNodeContainer= null;
        if(yangDataContainer instanceof YangDataDocument){
            // For YangDataDocument, we need to find the schema node that matches the root element
            YangSchemaContext schemaContext = ((YangDataDocument) yangDataContainer).getSchemaContext();
            org.yangcentral.yangkit.common.api.QName rootQName = ((YangDataDocument) yangDataContainer).getQName();
                
            // Try to find the matching top-level schema node
            SchemaNode rootSchemaNode = schemaContext.getTreeNodeChild(rootQName);
            System.out.println("[DEBUG] Looking for root schema node: " + rootQName.getLocalName());
            System.out.println("[DEBUG] Found root schema node: " + (rootSchemaNode != null ? rootSchemaNode.getIdentifier().getLocalName() : "null"));
                
            if (rootSchemaNode instanceof SchemaNodeContainer) {
                schemaNodeContainer = (SchemaNodeContainer) rootSchemaNode;
                System.out.println("[DEBUG] Using root schema node's children");
            } else {
                schemaNodeContainer = schemaContext;
                System.out.println("[DEBUG] Root schema node not found or not a container, using schema context");
            }
        }else {
            YangData<?> yangData = (YangData<?>) yangDataContainer;
            schemaNodeContainer = (SchemaNodeContainer) yangData.getSchemaNode();
            System.out.println("[DEBUG] Getting schema nodes from data's schema node");
        }
        List<Element> children = element.elements();
        System.out.println("[DEBUG] Found " + children.size() + " XML child elements");
        for (Element child : children) {
            System.out.println("[DEBUG] Processing XML child: " + child.getQName().getName() + " namespace: " + child.getNamespaceURI());
            SchemaNode sonSchemaNode = schemaNodeContainer.getTreeNodeChild(
                    new org.yangcentral.yangkit.common.api.QName(child.getNamespaceURI(),
                            child.getNamespacePrefix(),child.getQName().getName()));
            System.out.println("[DEBUG] Found schema node: " + (sonSchemaNode != null ? sonSchemaNode.getIdentifier().getLocalName() : "null"));

            if (sonSchemaNode == null) {
                // Check if this is an augmented node or unknown element
                continue;
            }
            
            // Filter non-config data if onlyConfig is true
            if (onlyConfig && !isConfigTrue(sonSchemaNode)) {
                System.out.println("[DEBUG] Skipping non-config node: " + sonSchemaNode.getIdentifier().getLocalName());
                continue; // Skip non-config data
            }
            
            System.out.println("[DEBUG] Processing child node: " + sonSchemaNode.getIdentifier().getLocalName() + " (type: " + sonSchemaNode.getClass().getSimpleName() + ")");
            YangDataXmlCodec xmlCodec = YangDataXmlCodec.getInstance(sonSchemaNode);
            System.out.println("[DEBUG] Created codec instance: " + (xmlCodec != null ? xmlCodec.getClass().getSimpleName() : "null"));
            if (xmlCodec != null) {
                // Use the local validatorResultBuilder, not pass a new one
                System.out.println("[DEBUG] Calling deserialize for node: " + sonSchemaNode.getIdentifier().getLocalName());
                YangData<?> childData = xmlCodec.deserialize(child, validatorResultBuilder);
                if (childData != null) {
                    System.out.println("[DEBUG] Child data created successfully: " + childData.getClass().getSimpleName());
                    try {
                        yangDataContainer.addDataChild(childData);
                    } catch (org.yangcentral.yangkit.data.api.exception.YangDataException e) {
                        // Log error but continue processing
                        System.err.println("Warning: Failed to add child data: " + e.getMessage());
                    }
                } else {
                    System.out.println("[DEBUG] Child data is null (validation error should have been recorded)");
                }
            }
        }
        return validatorResultBuilder.build();
    }
    
    @Override
    public YangDataDocument deserialize(Element root, ValidatorResultBuilder validatorResultBuilder) {
        if (null == root) {
            return null;
        }
        System.out.println("[DEBUG] YangDataDocumentXmlCodec.deserialize called with root element: " + root.getQName().getName());
        org.yangcentral.yangkit.common.api.QName docQName = Converter.convert(root.getQName());

        YangDataDocument yangDataDocument = new YangDataDocumentImpl(docQName, schemaContext);
        System.out.println("[DEBUG] Created YangDataDocument: " + yangDataDocument.getQName().getLocalName());
        processAttributers(yangDataDocument, root);
        YangDataContainer yangDataContainer = yangDataDocument;
        System.out.println("[DEBUG] Calling buildChildrenData...");
        validatorResultBuilder.merge(buildChildrenData(yangDataContainer,root));
        System.out.println("[DEBUG] buildChildrenData completed");

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

