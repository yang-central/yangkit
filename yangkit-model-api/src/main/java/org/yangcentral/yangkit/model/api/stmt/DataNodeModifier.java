package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.model.api.schema.SchemaPath;

public interface DataNodeModifier {
   SchemaNode getTarget();

   void setTarget(SchemaNode var1);

   SchemaPath getTargetPath();

   void setTargetPath(SchemaPath var1);
}
