package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import java.util.List;
import org.jaxen.JaxenException;

public interface YangData<S extends SchemaNode> extends Comparable<YangData>, Cloneable {
   QName getQName();

   S getSchemaNode();

   YangDataContainer getParent();

   YangDataContainer getDataParent();

   void setParent(YangDataContainer var1);

   YangDataDocument getDocument();

   void setDocument(YangDataDocument var1);

   boolean isRoot();

   void setRoot(boolean var1);

   void detach();

   void update();

   default boolean isVirtual() {
      return false;
   }

   void addAttribute(Attribute var1);

   Attribute getAttribute(QName var1);

   List<Attribute> getAttributes(String var1);

   void deleteAttribute(QName var1);

   List<Attribute> getAttributes();

   void setAttributes(List<Attribute> var1);

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

   void merge(YangData<?> var1, boolean var2) throws YangDataException;

   default void merge(YangData<?> candidate) throws YangDataException {
      this.merge(candidate, true);
   }

   boolean isConfig();

   boolean isDummyNode();

   void setDummyNode(boolean var1);
}
