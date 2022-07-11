package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface SchemaNodeContainer {
   List<SchemaNode> getSchemaNodeChildren();

   ValidatorResult addSchemaNodeChild(SchemaNode schemaNode);

   ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes);

   SchemaNode getSchemaNodeChild(QName identifier);

   DataNode getDataNodeChild(QName identifier);

   List<DataNode> getDataNodeChildren();

   default boolean isSchemaTreeRoot() {
      return false;
   }

   void removeSchemaNodeChild(QName identifier);

   void removeSchemaNodeChild(SchemaNode schemaNode);

   SchemaNode getMandatoryDescendant();
}
