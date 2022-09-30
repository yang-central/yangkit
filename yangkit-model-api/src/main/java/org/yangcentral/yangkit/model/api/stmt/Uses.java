package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Uses extends DataDefinition, WhenSupport, VirtualSchemaNode, IdentifierRef,YangBuiltinStatement {
   Grouping getRefGrouping();

   List<Augment> getAugments();

   List<Refine> getRefines();
}
