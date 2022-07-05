package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Namespace;

import java.net.URI;

public class NamespaceImpl extends YangSimpleStatementImpl implements Namespace {
   public NamespaceImpl(String argStr) {
      super(argStr);
   }

   public URI getUri() {
      return URI.create(this.getArgStr());
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.NAMESPACE.getQName();
   }
}
