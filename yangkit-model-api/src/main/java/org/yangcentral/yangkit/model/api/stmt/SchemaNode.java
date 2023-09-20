package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;

public interface SchemaNode extends Entity {
   SchemaPath.Absolute getSchemaPath();

   SchemaNodeContainer getParentSchemaNode();

   boolean supported();

   boolean isConfig();

   void setSupported(boolean var1);

   void setParentSchemaNode(SchemaNodeContainer schemaNodeContainer);

   SchemaNodeContainer getClosestAncestorNode();

   boolean isMandatory();

   boolean hasDefault();

   SchemaTreeType getSchemaTreeType();

   void setSchemaTreeType(SchemaTreeType treeType);

   QName getIdentifier();

   default String getJsonIdentifier() {
      return getContext().getCurModule().getMainModule().getArgStr()
              + ":"
              + getIdentifier().getLocalName();
   }

   boolean isAncestorNode(SchemaNode ancestor);

   default boolean isActive() {
      if (!this.supported()) {
         return false;
      } else if (this instanceof IfFeatureSupport && !((IfFeatureSupport)this).evaluateFeatures()) {
         return false;
      } else if (this.getParentSchemaNode() instanceof SchemaNode) {
         SchemaNode parentSchemaNode = (SchemaNode)this.getParentSchemaNode();
         return parentSchemaNode.isActive();
      } else {
         return true;
      }
   }

   boolean isDeviated();

   void setDeviated(boolean deviated);

   SchemaNodeContainer getSchemaTreeRoot();
}
