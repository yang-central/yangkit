package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.base.ValidatePhase;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

public interface YangDataContainer{
   List<YangData<?>> getChildren();

   YangData<?> getChild(DataIdentifier identifier);

   List<YangData<?>> getChildren(QName qName);

   List<YangData<? extends DataNode>> getDataChildren();

   YangData<? extends DataNode> getDataChild(DataIdentifier identifier);

   List<YangData<? extends DataNode>> getDataChildren(QName qName);

   List<YangData<? extends DataNode>> getDataChildren(String name);

   List<YangData<? extends DataNode>> getDataChildren(String name, String namespace);

   default void addChild(YangData<?> child) throws YangDataException {
      this.addChild(child, true);
   }

   void addChild(YangData<?> child, boolean autoDelete) throws YangDataException;

   YangData<?> removeChild(DataIdentifier identifier);

   void addDataChild(YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException;

   default void addDataChild(YangData<? extends DataNode> child) throws YangDataException {
      this.addDataChild(child, true);
   }

   YangData<? extends DataNode> removeDataChild(DataIdentifier identifier);

   ValidatorResult validateChildren();

   List<YangDataCompareResult> compareChildren(YangDataContainer another);
}
