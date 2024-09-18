package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;

public interface YangDataCompareResult {
   AbsolutePath getPath();

   DifferenceType getDifferenceType();

   YangData<?> getChanged();

   YangData<?> getPrevious();
}
