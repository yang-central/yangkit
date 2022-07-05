package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.common.api.QName;

public class YangParentStatementInfo {
   private QName parentYangKeyword;
   private Cardinality cardinality;

   public YangParentStatementInfo(QName parentYangKeyword, Cardinality cardinality) {
      this.parentYangKeyword = parentYangKeyword;
      this.cardinality = cardinality;
   }

   public QName getParentYangKeyword() {
      return this.parentYangKeyword;
   }

   public Cardinality getCardinality() {
      return this.cardinality;
   }
}
