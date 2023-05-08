package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

public interface YangDataOperator extends Cloneable {
   YangDataContainer getOperatedData();

   void create(YangData<?> node, boolean autoDelete) throws YangDataException;

   void merge(YangData<?> node, boolean autoDelete) throws YangDataException;

   void replace(YangData<?> node, boolean autoDelete) throws YangDataException;

   default void create(YangData<?> node) throws YangDataException {
      create(node,true);
   }

   default void merge(YangData<? extends DataNode> node) throws YangDataException {
      merge(node,true);
   }

   default void replace(YangData<? extends DataNode> node) throws YangDataException {
      replace(node,true);
   }

   void delete(DataIdentifier identifier);
}
