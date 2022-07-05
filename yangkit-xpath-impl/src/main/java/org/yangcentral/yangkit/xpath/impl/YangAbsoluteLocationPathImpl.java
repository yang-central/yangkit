package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import java.util.Collections;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.util.SingletonList;
import org.yangcentral.yangkit.xpath.YangContextSupport;

public class YangAbsoluteLocationPathImpl extends YangLocationPathImpl {
   public boolean isAbsolute() {
      return true;
   }

   public String getText() {
      return "/" + super.getText();
   }

   public Object evaluate(Context context) throws JaxenException {
      YangContextSupport support = (YangContextSupport)context.getContextSupport();
      Navigator nav = support.getNavigator();
      Context absContext = new Context(support);
      List contextNodes = context.getNodeSet();
      if (contextNodes.isEmpty()) {
         return Collections.EMPTY_LIST;
      } else {
         Object firstNode = contextNodes.get(0);
         Object docNode = nav.getDocumentNode(firstNode);
         if (docNode == null) {
            return Collections.EMPTY_LIST;
         } else {
            List list = new SingletonList(docNode);
            absContext.setNodeSet(list);
            return super.evaluate(absContext);
         }
      }
   }

   public String toString() {
      return "[(YangAbsoluteLocationPath): " + super.toString() + "]";
   }

   public boolean isInstanceIdentifier(YangSchemaContext schemaContext) {
      return false;
   }
}
