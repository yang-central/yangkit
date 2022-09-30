package org.yangcentral.yangkit.model.api.stmt;

public interface Container extends ContainerDataNode,YangBuiltinStatement {
   Presence getPresence();

   void setPresence(Presence presence);

   boolean isPresence();
}
