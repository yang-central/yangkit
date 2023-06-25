package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.operation.DataChangeNotifier;
import org.yangcentral.yangkit.data.api.operation.YangDataDocumentOperator;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

public interface YangDataDocument extends YangDataContainer,YangDataEntity<YangDataDocument>{
   YangSchemaContext getSchemaContext();

   void setOnlyConfig(boolean onlyConfig);

   boolean onlyConfig();
}
