package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

public interface YangDataContainer{
   List<YangData<?>> getChildren();

   YangData<?> getChild(DataIdentifier identifier);

   List<YangData<?>> getChildren(QName qName);

   List<YangData<?>> getDataChildren();

   YangData<?> getDataChild(DataIdentifier identifier);

   List<YangData<?>> getDataChildren(QName qName);

   List<YangData<?>> getDataChildren(String name);

   List<YangData<?>> getDataChildren(String name, String namespace);

   default void addChild(YangData<?> child) throws YangDataException {
      this.addChild(child, true);
   }

   void addChild(YangData<?> child, boolean autoDelete) throws YangDataException;

   YangData<?> removeChild(DataIdentifier identifier);

   void addDataChild(YangData<?> child, boolean autoDelete) throws YangDataException;

   default void addDataChild(YangData<?> child) throws YangDataException {
      this.addDataChild(child, true);
   }

   YangData<?> removeDataChild(DataIdentifier identifier);

   ValidatorResult validateChildren();

   List<YangDataCompareResult> compareChildren(YangDataContainer another);
}
