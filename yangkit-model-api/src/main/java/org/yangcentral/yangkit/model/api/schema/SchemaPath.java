package org.yangcentral.yangkit.model.api.schema;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;

import java.util.List;

public interface SchemaPath {
   QName getLast();

   String toString();

   List<QName> getPath();

   void addStep(QName var1);

   boolean isAbsolute();

   SchemaNode getSchemaNode(YangSchemaContext var1);

   public interface Descendant extends SchemaPath {
      SchemaNodeContainer getContext();
   }

   public interface Absolute extends SchemaPath {
      boolean contains(Absolute var1);

      List<QName> getRelativeSchemaPath(Absolute var1);
   }
}
