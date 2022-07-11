package org.yangcentral.yangkit.model.api.schema;
/**
 * the interface of datastore schema
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface DataStoreSchema {
   String getName();

   YangSchema getSchema();
}
