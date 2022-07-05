package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

public interface YangDataContainer {
   List<YangData<?>> getChildren();

   YangData<?> getChild(DataIdentifier var1);

   List<YangData<?>> getChildren(QName var1);

   List<YangData<? extends DataNode>> getDataChildren();

   YangData<? extends DataNode> getDataChild(DataIdentifier var1);

   List<YangData<? extends DataNode>> getDataChildren(QName var1);

   List<YangData<? extends DataNode>> getDataChildren(String var1);

   List<YangData<? extends DataNode>> getDataChildren(String var1, String var2);

   default void addChild(YangData<?> child) throws YangDataException {
      this.addChild(child, true);
   }

   void addChild(YangData<?> var1, boolean var2) throws YangDataException;

   YangData<?> removeChild(DataIdentifier var1);

   void addDataChild(YangData<? extends DataNode> var1, boolean var2) throws YangDataException;

   default void addDataChild(YangData<? extends DataNode> child) throws YangDataException {
      this.addDataChild(child, true);
   }

   YangData<? extends DataNode> removeDataChild(DataIdentifier var1);
}
