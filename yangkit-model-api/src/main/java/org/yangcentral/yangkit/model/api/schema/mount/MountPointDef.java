package org.yangcentral.yangkit.model.api.schema.mount;

import org.yangcentral.yangkit.model.api.schema.YangLibrary;

public interface MountPointDef {
   String getModuleName();

   String getLabel();

   YangLibrary getYangLibrary();
}
