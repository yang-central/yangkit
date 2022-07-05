package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class YangStatementParserPolicy {
   private QName keyword;
   private Class<? extends YangStatement> clazz;
   private List<BuildPhase> phases = new ArrayList();

   public YangStatementParserPolicy(QName keyword, Class<? extends YangStatement> clazz, List<BuildPhase> phases) {
      this.keyword = keyword;
      this.clazz = clazz;
      this.phases.addAll(phases);
   }

   public YangStatementParserPolicy(QName keyword, Class<? extends YangStatement> clazz) {
      this.keyword = keyword;
      this.clazz = clazz;
   }

   public QName getKeyword() {
      return this.keyword;
   }

   public List<BuildPhase> getPhases() {
      return this.phases;
   }

   public Class<? extends YangStatement> getClazz() {
      return this.clazz;
   }

   public boolean isLastPhase(BuildPhase phase) {
      if (phase == null) {
         return false;
      } else {
         return this.phases.lastIndexOf(phase) == this.phases.size() - 1;
      }
   }
}
