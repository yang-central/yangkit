package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.data.api.model.YangData;
import java.util.Iterator;
import org.jaxen.ContextSupport;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.expr.iter.IterableChildAxis;
import org.yangcentral.yangkit.xpath.YangContextSupport;

public class YangIterableChildAxis extends IterableChildAxis {
   public YangIterableChildAxis(int value) {
      super(value);
   }

   public Iterator namedAccessIterator(Object contextNode, ContextSupport support, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException {
      if (namespacePrefix == null || namespacePrefix.length() == 0) {
         YangContextSupport yangContextSupport = (YangContextSupport)support;
         Object contextData = yangContextSupport.getContextData();
         if (contextData instanceof YangData) {
            YangData<?> yangData = (YangData)contextData;
            namespacePrefix = yangData.getQName().getPrefix();
            namespaceURI = yangData.getQName().getNamespace().toString();
         }
      }

      return super.namedAccessIterator(contextNode, support, localName, namespacePrefix, namespaceURI);
   }
}
