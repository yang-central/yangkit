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

public interface YangDataDocument extends YangDataContainer, Cloneable {
   QName getQName();

   YangSchemaContext getSchemaContext();

   ValidatorResult validate();


   void addAttribute(Attribute var1);

   Attribute getAttribute(QName var1);

   List<Attribute> getAttributes();

   void setAttributes(List<Attribute> var1);

   List<YangDataCompareResult> compare(YangDataDocument another);

   ValidatorResult processWhen();

   YangDataDocument clone() throws CloneNotSupportedException;

   void update();
}
