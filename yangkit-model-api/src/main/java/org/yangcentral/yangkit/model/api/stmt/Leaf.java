package org.yangcentral.yangkit.model.api.stmt;

public interface Leaf extends TypedDataNode, MandatorySupport {
   Default getDefault();

   Default getEffectiveDefault();

   void setDefault(Default var1);

   boolean isKey();

   void setKey(boolean var1);
}
