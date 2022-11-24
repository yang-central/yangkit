package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.base.YangStatementChecker;
import org.yangcentral.yangkit.common.api.QName;

public class YangParentStatementInfo {
   private QName parentYangKeyword;
   private Cardinality cardinality;

   private Class<? extends YangStatementChecker> checker;
   public YangParentStatementInfo(QName parentYangKeyword, Cardinality cardinality) {
      this.parentYangKeyword = parentYangKeyword;
      this.cardinality = cardinality;
   }

   public YangParentStatementInfo(QName parentYangKeyword, Cardinality cardinality,
                                  Class<? extends YangStatementChecker> checker) {
      this.parentYangKeyword = parentYangKeyword;
      this.cardinality = cardinality;
      this.checker = checker;
   }
   public Class<? extends YangStatementChecker> getChecker() {
      return checker;
   }

   public void setChecker(Class<? extends YangStatementChecker> checker) {
      this.checker = checker;
   }
   public QName getParentYangKeyword() {
      return this.parentYangKeyword;
   }

   public Cardinality getCardinality() {
      return this.cardinality;
   }
}
