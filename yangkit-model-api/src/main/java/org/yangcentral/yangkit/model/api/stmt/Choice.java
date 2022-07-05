package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Choice extends Identifiable, Augmentable, MandatorySupport, SchemaDataNode, SchemaNodeContainer, DataDefContainer {
   Default getDefault();

   boolean setDefault(Default var1);

   Case getDefaultCase();

   boolean setDefaultCase(Case var1);

   List<Case> getCases();

   boolean addCase(Case var1);
}
