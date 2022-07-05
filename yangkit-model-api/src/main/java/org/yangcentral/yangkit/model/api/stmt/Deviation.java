package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Deviation extends YangBuiltinStatement, MetaDef, DataNodeModifier {
   List<Deviate> getDeviates();
}
