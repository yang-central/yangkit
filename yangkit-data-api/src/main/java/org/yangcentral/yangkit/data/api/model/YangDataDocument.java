package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface YangDataDocument extends YangDataContainer,YangDataEntity<YangDataDocument>{
   YangSchemaContext getSchemaContext();

   void setOnlyConfig(boolean onlyConfig);

   boolean onlyConfig();

   default String getPartialDataFilter() {
      return null;
   }

   default void setPartialDataFilter(String filter) {
      if (filter != null) {
         throw new UnsupportedOperationException("Filtered partial-data validation is not supported");
      }
   }

   String getDocString();

   String[] getModulesStrings();

}
