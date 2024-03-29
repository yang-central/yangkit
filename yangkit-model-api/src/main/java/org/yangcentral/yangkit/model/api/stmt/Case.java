package org.yangcentral.yangkit.model.api.stmt;

public interface Case extends Identifiable, SchemaNode, SchemaNodeContainer,
        DataDefContainer, DataDefinition, Augmentable,YangBuiltinStatement {
   boolean isShortCase();

   void setShortCase(boolean bool);

   Choice getParent();

   void setParent(Choice parent);
}
