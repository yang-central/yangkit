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

   NamespaceContextDom4j getNamespaceContext();

   ValidatorResult validate();

   YangData<? extends DataNode> find(AbsolutePath var1);

   void addAttribute(Attribute var1);

   Attribute getAttribute(QName var1);

   List<Attribute> getAttributes();

   void setAttributes(List<Attribute> var1);

   List<YangCompareResult> compare(YangDataDocument var1);

   DataChangeNotifier getDataChangeNotifier();

   void setDataChangeNotifier(DataChangeNotifier var1);

   YangDataDocumentOperator getOperator();

   void setOperator(YangDataDocumentOperator var1);

   boolean isOnlyConfig();

   void setOnlyConfig(boolean var1);

   ValidatorResult processWhen();

   YangDataDocument clone() throws CloneNotSupportedException;

   void update();
}
