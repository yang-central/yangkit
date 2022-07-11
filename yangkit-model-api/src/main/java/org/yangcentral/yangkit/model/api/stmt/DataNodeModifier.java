package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.model.api.schema.SchemaPath;

public interface DataNodeModifier {
   SchemaNode getTarget();

   void setTarget(SchemaNode target);

   SchemaPath getTargetPath();

   void setTargetPath(SchemaPath schemaPath);
}
