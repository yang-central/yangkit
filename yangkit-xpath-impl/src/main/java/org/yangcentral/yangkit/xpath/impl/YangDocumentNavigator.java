package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Module;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;

public class YangDocumentNavigator extends DefaultNavigator implements NamedAccessNavigator {
   private static YangDocumentNavigator instance = new YangDocumentNavigator();

   private YangDocumentNavigator() {
   }

   public static YangDocumentNavigator getInstance() {
      return instance;
   }

   public String getElementNamespaceUri(Object element) {
      YangData<?> yangData = (YangData)element;
      return yangData.getQName().getNamespace().toString();
   }

   public String getElementName(Object element) {
      YangData<?> yangData = (YangData)element;
      return yangData.getQName().getLocalName();
   }

   public String getElementQName(Object element) {
      YangData<?> yangData = (YangData)element;
      return yangData.getQName().getQualifiedName();
   }

   public String getAttributeNamespaceUri(Object attr) {
      Attribute attribute = (Attribute)attr;
      return attribute.getName().getNamespace().toString();
   }

   public String getAttributeName(Object attr) {
      Attribute attribute = (Attribute)attr;
      return attribute.getName().getLocalName();
   }

   public String getAttributeQName(Object attr) {
      Attribute attribute = (Attribute)attr;
      return attribute.getName().getQualifiedName();
   }

   public boolean isDocument(Object object) {
      return object instanceof YangDataDocument;
   }

   public boolean isElement(Object object) {
      return object instanceof YangData;
   }

   public boolean isAttribute(Object object) {
      return object instanceof Attribute;
   }

   public boolean isNamespace(Object object) {
      return object instanceof Namespace;
   }

   public boolean isComment(Object object) {
      return false;
   }

   public boolean isText(Object object) {
      return object instanceof String;
   }

   public boolean isProcessingInstruction(Object object) {
      return false;
   }

   public String getCommentStringValue(Object comment) {
      return null;
   }

   public String getElementStringValue(Object element) {
      YangData<?> yangData = (YangData)element;
      if (!(yangData instanceof TypedData)) {
         return null;
      } else {
         TypedData typedData = (TypedData)yangData;
         return typedData.getStringValue();
      }
   }

   public String getAttributeStringValue(Object attr) {
      Attribute attribute = (Attribute)attr;
      return attribute.getValue();
   }

   public String getNamespaceStringValue(Object ns) {
      Namespace namespace = (Namespace)ns;
      return namespace.getUri().toString();
   }

   public String getTextStringValue(Object text) {
      return (String)text;
   }

   public String getNamespacePrefix(Object ns) {
      Namespace namespace = (Namespace)ns;
      return namespace.getPrefix();
   }

   public XPath parseXPath(String xpath) throws SAXPathException {
      return new YangXPathImpl(xpath);
   }

   public Iterator getChildAxisIterator(Object contextNode) throws UnsupportedAxisException {
      if (!(contextNode instanceof YangDataContainer)) {
         return JaxenConstants.EMPTY_ITERATOR;
      } else {
         YangDataContainer dataContainer = (YangDataContainer)contextNode;
         List<YangData<? extends DataNode>> children = dataContainer.getDataChildren();
         return children != null && !children.isEmpty() ? children.iterator() : JaxenConstants.EMPTY_ITERATOR;
      }
   }

   public Iterator getDescendantAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getDescendantAxisIterator(contextNode);
   }

   public Iterator getParentAxisIterator(Object contextNode) throws UnsupportedAxisException {
      if (contextNode instanceof YangDataDocument) {
         return JaxenConstants.EMPTY_ITERATOR;
      } else if (!(contextNode instanceof YangData)) {
         throw new UnsupportedAxisException("un-supported context node.");
      } else {
         YangData<?> context = (YangData)contextNode;
         YangDataContainer parent = context.getContext().getDataParent();
         return parent instanceof YangDataDocument ? Arrays.asList(context.getContext().getDocument()).iterator() : Arrays.asList(parent).iterator();
      }
   }

   public Iterator getAncestorAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getAncestorAxisIterator(contextNode);
   }

   public Iterator getFollowingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getFollowingSiblingAxisIterator(contextNode);
   }

   public Iterator getPrecedingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getPrecedingSiblingAxisIterator(contextNode);
   }

   public Iterator getFollowingAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getFollowingAxisIterator(contextNode);
   }

   public Iterator getPrecedingAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getPrecedingAxisIterator(contextNode);
   }

   public Iterator getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException {
      YangData<?> context = (YangData)contextNode;
      List<Attribute> attributes = context.getAttributes();
      return null != attributes && !attributes.isEmpty() ? attributes.iterator() : JaxenConstants.EMPTY_ITERATOR;
   }

   public Iterator getNamespaceAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getNamespaceAxisIterator(contextNode);
   }

   public Iterator getSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getSelfAxisIterator(contextNode);
   }

   public Iterator getDescendantOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getDescendantOrSelfAxisIterator(contextNode);
   }

   public Iterator getAncestorOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
      return super.getAncestorOrSelfAxisIterator(contextNode);
   }

   public Object getDocumentNode(Object contextNode) {
      YangData<?> context = (YangData)contextNode;
      return context.getContext().getDocument();
   }

   public String translateNamespacePrefixToUri(String prefix, Object element) {
      YangData<?> context = (YangData)element;
      ModuleId moduleId = (ModuleId)context.getSchemaNode().getContext().getCurModule().getPrefixes().get(prefix);
      if (moduleId == null) {
         return null;
      } else {
         YangSchemaContext schemaContext = context.getSchemaNode().getContext().getSchemaContext();
         Optional<Module> moduleOptional = schemaContext.getModule(moduleId);
         if (!moduleOptional.isPresent()) {
            return null;
         } else {
            Module module = (Module)moduleOptional.get();
            return module.getMainModule().getNamespace().getUri().toString();
         }
      }
   }

   public String getProcessingInstructionTarget(Object obj) {
      return super.getProcessingInstructionTarget(obj);
   }

   public String getProcessingInstructionData(Object obj) {
      return super.getProcessingInstructionData(obj);
   }

   public short getNodeType(Object node) {
      return super.getNodeType(node);
   }

   public Object getParentNode(Object contextNode) throws UnsupportedAxisException {
      return super.getParentNode(contextNode);
   }

   public Object getDocument(String url) throws FunctionCallException {
      return super.getDocument(url);
   }

   public Object getElementById(Object contextNode, String elementId) {
      return super.getElementById(contextNode, elementId);
   }

   public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException {
      if (!(contextNode instanceof YangDataContainer)) {
         return JaxenConstants.EMPTY_ITERATOR;
      } else {
         YangDataContainer yangDataContainer = (YangDataContainer)contextNode;
         QName childQName = null;
         YangData yangData;
         if (namespacePrefix.length() == 0) {
            if (yangDataContainer instanceof YangData) {
               yangData = (YangData)yangDataContainer;
               childQName = new QName(yangData.getQName().getNamespace(), localName);
            }
         } else {
            childQName = new QName(namespaceURI, namespacePrefix, localName);
         }

         yangData = null;
         List children;
         if (childQName != null) {
            children = yangDataContainer.getDataChildren(childQName);
         } else {
            children = yangDataContainer.getDataChildren(localName);
         }

         return children != null && !children.isEmpty() ? children.iterator() : JaxenConstants.EMPTY_ITERATOR;
      }
   }

   public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException {
      YangData<?> contextData = (YangData)contextNode;
      Attribute attribute = contextData.getAttribute(new QName(namespaceURI, namespacePrefix, localName));
      return null == attribute ? JaxenConstants.EMPTY_ITERATOR : Arrays.asList(attribute).iterator();
   }
}
