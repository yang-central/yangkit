package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface YangDataDocument extends YangDataContainer,YangDataEntity<YangDataDocument>{
   YangSchemaContext getSchemaContext();

   void setOnlyConfig(boolean onlyConfig);

   boolean onlyConfig();

   String getDocString();

   String[] getModulesStrings();

}
