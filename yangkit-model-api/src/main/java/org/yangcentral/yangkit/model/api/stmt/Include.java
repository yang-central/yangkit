package org.yangcentral.yangkit.model.api.stmt;

import java.util.Optional;

public interface Include extends YangBuiltinStatement, MetaDef, Identifiable {
   RevisionDate getRevisionDate();

   Optional<SubModule> getInclude();
}
