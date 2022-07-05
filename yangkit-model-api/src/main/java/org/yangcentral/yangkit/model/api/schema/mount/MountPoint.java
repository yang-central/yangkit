package org.yangcentral.yangkit.model.api.schema.mount;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Identifiable;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

public interface MountPoint extends Identifiable, YangUnknown {
   YangSchemaContext getYangSchemaContext();
}
