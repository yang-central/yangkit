package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

public interface DataChangeListener {
   void processDataChange(YangDataDocument document, AbsolutePath path, DataChangeType dataChangeType,
                          YangData<?> oldData, YangData<?> newData);
}
