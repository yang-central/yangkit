package org.yangcentral.yangkit.model.api.stmt;

public interface Entity extends YangStatement, MetaDef {
   StatusStmt getStatus();

   Status getEffectiveStatus();
}
