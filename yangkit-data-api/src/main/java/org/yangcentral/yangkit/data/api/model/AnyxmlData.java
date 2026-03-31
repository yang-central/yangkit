package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Anyxml;
import org.dom4j.Document;

public interface AnyxmlData extends YangData<Anyxml> {
   Document getValue();

   default Document getEffectiveValue() {
      return getValue();
   }

   void setValue(Document var1);
}
