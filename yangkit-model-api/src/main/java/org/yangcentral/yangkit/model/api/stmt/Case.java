package org.yangcentral.yangkit.model.api.stmt;

public interface Case extends Identifiable, SchemaNode, SchemaNodeContainer, DataDefContainer, DataDefinition, Augmentable {
   boolean isShortCase();

   void setShortCase(boolean var1);

   Choice getParent();

   void setParent(Choice var1);
}
