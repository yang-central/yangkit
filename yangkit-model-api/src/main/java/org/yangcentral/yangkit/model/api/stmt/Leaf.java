package org.yangcentral.yangkit.model.api.stmt;

public interface Leaf extends TypedDataNode, MandatorySupport {
   Default getDefault();

   Default getEffectiveDefault();

   void setDefault(Default aDefault);

   boolean isKey();

   void setKey(boolean key);
}
