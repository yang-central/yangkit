package org.yangcentral.yangkit.model.api.stmt;

public interface YangUnknown extends YangStatement {
   String getKeyword();

   Extension getExtension();
}
