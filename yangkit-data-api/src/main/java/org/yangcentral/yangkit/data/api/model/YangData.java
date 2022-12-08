package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.base.YangDataContext;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import java.util.List;
import org.jaxen.JaxenException;

public interface YangData<S extends SchemaNode> extends Comparable<YangData>, Cloneable {
   QName getQName();

   S getSchemaNode();

   YangDataContext getContext();



   void setContext(YangDataContext context);


   boolean isRoot();


   void detach();

   void update();

   default boolean isVirtual() {
      return false;
   }

   void addAttribute(Attribute attribute);

   Attribute getAttribute(QName qName);

   List<Attribute> getAttributes(String name);

   void deleteAttribute(QName qName);

   List<Attribute> getAttributes();

   void setAttributes(List<Attribute> attributes);

   DataIdentifier getIdentifier();

   AbsolutePath getPath();

   boolean checkWhen() throws JaxenException;

   ValidatorResult validate();

   ValidatorResult processWhen();

   default int compareTo(YangData o) {
      return this.getIdentifier().compareTo(o.getIdentifier());
   }

   YangData<S> clone() throws CloneNotSupportedException;

   List<YangCompareResult> compare(YangData<?> var1);


   boolean isConfig();

   boolean isDummyNode();

   void setDummyNode(boolean bool);
}
