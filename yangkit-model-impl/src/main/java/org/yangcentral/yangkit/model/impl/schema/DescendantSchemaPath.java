package org.yangcentral.yangkit.model.impl.schema;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;

import java.util.List;

public class DescendantSchemaPath extends SchemaPathImpl implements SchemaPath.Descendant {
   private SchemaNodeContainer context;

   public DescendantSchemaPath(List<QName> steps, SchemaNodeContainer context) {
      super(steps);
      this.context = context;
   }

   public DescendantSchemaPath(SchemaNodeContainer context) {
      this.context = context;
   }

   public SchemaNodeContainer getContext() {
      return this.context;
   }

   public boolean isAbsolute() {
      return false;
   }
}
