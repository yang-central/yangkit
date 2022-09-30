package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Choice extends Identifiable, Augmentable, MandatorySupport, SchemaDataNode,
        SchemaNodeContainer, DataDefContainer,YangBuiltinStatement {
   Default getDefault();

   boolean setDefault(Default aDefault);

   Case getDefaultCase();

   boolean setDefaultCase(Case defaultCase);

   List<Case> getCases();

   boolean addCase(Case aCase);
}
