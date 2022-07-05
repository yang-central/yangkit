package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Rpc;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import java.net.URI;
import java.util.List;

public class XPathUtil {
   public static Object getXPathContextNode(SchemaNode current) {
      if (current == null) {
         return null;
      } else if (!(current instanceof DataNode) && !(current instanceof Rpc) && !(current instanceof Action) && !(current instanceof Notification)) {
         SchemaNodeContainer closestAncestorNode = current.getClosestAncestorNode();
         return closestAncestorNode instanceof Module ? new YangXPathRoot((Module)closestAncestorNode) : closestAncestorNode;
      } else {
         return current;
      }
   }

   public static Identity getIdentity(YangSchemaContext schemaContext, String uri, String identityName) {
      List<Module> contextModules = schemaContext.getModule(URI.create(uri));
      if (contextModules.isEmpty()) {
         return null;
      } else {
         Module curModule = (Module)contextModules.get(0);
         Identity destIdentity = curModule.getIdentity(identityName);
         return destIdentity;
      }
   }

   public static YangSchemaContext getSchemaContext(Object context) {
      if (context instanceof YangData) {
         YangData yangData = (YangData)context;
         return yangData.getSchemaNode().getContext().getSchemaContext();
      } else if (context instanceof YangDataDocument) {
         YangDataDocument yangDataDocument = (YangDataDocument)context;
         return yangDataDocument.getSchemaContext();
      } else {
         throw new IllegalArgumentException("error type");
      }
   }
}
