package org.yangcentral.yangkit.model.api.stmt;

public interface Argument extends YangBuiltinStatement, Identifiable {
   boolean isYinElement();

   YinElement getYinElement();
}
