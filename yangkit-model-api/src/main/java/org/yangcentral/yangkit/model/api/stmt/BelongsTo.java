package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface BelongsTo extends YangBuiltinStatement, Identifiable {
   Prefix getPrefix();

   List<MainModule> getMainModules();
}
