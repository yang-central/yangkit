package org.yangcentral.yangkit.model.api.stmt;

public interface Container extends ContainerDataNode {
   Presence getPresence();

   void setPresence(Presence presence);

   boolean isPresence();
}
