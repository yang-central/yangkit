package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Refine extends YangBuiltinStatement, MetaDef, MustSupport, IfFeatureSupport, DataNodeModifier {
   Config getConfig();

   List<Default> getDefaults();

   Mandatory getMandatory();

   Presence getPresence();

   MinElements getMinElements();

   MaxElements getMaxElements();
}
