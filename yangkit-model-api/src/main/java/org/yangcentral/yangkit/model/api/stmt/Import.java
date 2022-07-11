package org.yangcentral.yangkit.model.api.stmt;

import java.util.Optional;

public interface Import extends YangBuiltinStatement, MetaDef, Identifiable {
   Prefix getPrefix();

   RevisionDate getRevisionDate();

   Optional<MainModule> getImport();

   boolean isReferenced();

   void setReferenced(boolean referenced);
}
