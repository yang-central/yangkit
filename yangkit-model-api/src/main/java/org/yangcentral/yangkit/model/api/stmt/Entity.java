package org.yangcentral.yangkit.model.api.stmt;

public interface Entity extends YangBuiltinStatement, MetaDef {
   StatusStmt getStatus();

   Status getEffectiveStatus();
}
