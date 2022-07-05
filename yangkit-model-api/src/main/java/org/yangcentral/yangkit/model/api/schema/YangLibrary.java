package org.yangcentral.yangkit.model.api.schema;

import java.util.List;

public interface YangLibrary {
   String getContentID();

   List<DataStore> getDatastores();
}
