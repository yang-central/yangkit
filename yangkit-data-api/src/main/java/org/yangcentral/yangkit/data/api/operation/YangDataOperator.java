package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

public interface YangDataOperator extends Cloneable {
   YangDataContainer getOperatedData();

   void create(YangData<? extends DataNode> var1) throws YangDataException;

   void merge(YangData<? extends DataNode> var1) throws YangDataException;

   void replace(YangData<? extends DataNode> var1) throws YangDataException;

   void delete(DataIdentifier var1);
}
