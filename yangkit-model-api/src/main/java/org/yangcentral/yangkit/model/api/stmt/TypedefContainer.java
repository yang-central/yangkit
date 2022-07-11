package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface TypedefContainer {
   List<Typedef> getTypedefs();

   Typedef getTypedef(int index);

   Typedef getTypedef(String name);
}
