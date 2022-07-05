package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface SchemaNodeContainer {
   List<SchemaNode> getSchemaNodeChildren();

   ValidatorResult addSchemaNodeChild(SchemaNode var1);

   ValidatorResult addSchemaNodeChildren(List<SchemaNode> var1);

   SchemaNode getSchemaNodeChild(QName var1);

   DataNode getDataNodeChild(QName var1);

   List<DataNode> getDataNodeChildren();

   default boolean isSchemaTreeRoot() {
      return false;
   }

   void removeSchemaNodeChild(QName var1);

   void removeSchemaNodeChild(SchemaNode var1);

   SchemaNode getMandatoryDescendant();
}
