package org.yangcentral.yangkit.model.api.stmt;

public interface MainModule extends Module {
   Namespace getNamespace();

   Prefix getPrefix();
}
