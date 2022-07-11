package org.yangcentral.yangkit.model.api.stmt;

public interface Default extends YangBuiltinStatement {
   Object getValue();

   void setValue(Object value);
}
