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

   /**
    * get all data node children including the augmented data nodes
    * @return list of data node child
    */
   List<DataNode> getDataNodeChildren();

   /**
    * get all schema node children except augmented schema nodes from other modules, inactive schema nodes, and uses should be expanded
    * @param ignoreNamespace whether ignore namespace
    * @return list of schema node child
    */
   List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace);

   default List<SchemaNode> getEffectiveSchemaNodeChildren(){
      return getEffectiveSchemaNodeChildren(false);
   }

   default boolean isSchemaTreeRoot() {
      return false;
   }

   void removeSchemaNodeChild(QName identifier);

   void removeSchemaNodeChild(SchemaNode schemaNode);

   SchemaNode getMandatoryDescendant();
}
