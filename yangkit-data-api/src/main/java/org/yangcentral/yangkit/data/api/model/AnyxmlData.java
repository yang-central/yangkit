package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Anyxml;
import org.dom4j.Document;

public interface AnyxmlData extends YangData<Anyxml> {
   Document getValue();

   void setValue(Document var1);
}
