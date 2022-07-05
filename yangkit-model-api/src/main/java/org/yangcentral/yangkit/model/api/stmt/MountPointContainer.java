package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.model.api.schema.mount.MountPoint;
import java.util.List;

interface MountPointContainer {
   List<MountPoint> getMountPoints();
}
