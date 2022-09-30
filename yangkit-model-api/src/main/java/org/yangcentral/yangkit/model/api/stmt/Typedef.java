package org.yangcentral.yangkit.model.api.stmt;

public interface Typedef extends Entity, Identifiable, Referencable,YangBuiltinStatement {
   Type getType();

   Units getUnits();

   Default getDefault();

   Default getEffectiveDefault();
}
