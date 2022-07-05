package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Anydata;

public interface AnyDataData extends YangData<Anydata> {
   YangDataDocument getValue();

   void setValue(YangDataDocument var1);
}
