package org.yangcentral.yangkit.model.api.stmt;

public interface Rpc extends Operation {
   default boolean isSchemaTreeRoot() {
      return true;
   }
}
