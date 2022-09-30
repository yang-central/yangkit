package org.yangcentral.yangkit.model.api.stmt;

public interface Leaf extends TypedDataNode, MandatorySupport,YangBuiltinStatement {
   Default getDefault();

   Default getEffectiveDefault();

   void setDefault(Default aDefault);

   boolean isKey();

   void setKey(boolean key);
}
