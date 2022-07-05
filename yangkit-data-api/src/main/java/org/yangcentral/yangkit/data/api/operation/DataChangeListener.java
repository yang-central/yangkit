package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

public interface DataChangeListener {
   void processDataChange(YangDataDocument var1, AbsolutePath var2, DataChangeType var3, YangData<?> var4, YangData<?> var5);
}
