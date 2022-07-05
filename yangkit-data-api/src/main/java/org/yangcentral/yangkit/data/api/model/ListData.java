package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.YangList;
import java.util.List;

public interface ListData extends YangData<YangList>, YangDataContainer {
   List<LeafData<?>> getKeys();
}
