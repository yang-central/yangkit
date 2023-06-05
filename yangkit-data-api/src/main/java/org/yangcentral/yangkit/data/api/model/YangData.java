package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.base.YangDataContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import java.util.List;
import org.jaxen.JaxenException;

public interface YangData<S extends SchemaNode> extends YangDataEntity<YangData> {

   S getSchemaNode();

   YangDataContext getContext();

   void setContext(YangDataContext context);

   boolean isRoot();

   void detach();

   default boolean isVirtual() {
      return false;
   }

   DataIdentifier getIdentifier();

   AbsolutePath getPath();

   boolean checkWhen() throws JaxenException;


   default int compareTo(YangData o) {
      return this.getIdentifier().compareTo(o.getIdentifier());
   }

   boolean isConfig();

   boolean isMandatory();

   boolean isDummyNode();

   void setDummyNode(boolean bool);
}
