package org.yangcentral.yangkit.model.api.schema;

import java.util.List;
/**
 * interface for yang library
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8525">yang library</a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangLibrary {
   String getContentID();

   List<DataStoreSchema> getDatastoreSchemas();
}
