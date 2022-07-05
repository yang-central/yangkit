package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YangUnknownParserPolicy extends YangStatementParserPolicy {
   private List<YangParentStatementInfo> parentStatements = new ArrayList();

   public YangUnknownParserPolicy(QName keyword, Class<? extends YangUnknown> clazz, List<BuildPhase> phases) {
      super(keyword, clazz, phases);
   }

   public YangUnknownParserPolicy(QName keyword, Class<? extends YangUnknown> clazz) {
      super(keyword, clazz);
   }

   public boolean addParentStatementInfo(YangParentStatementInfo parentStatementInfo) {
      Iterator var2 = this.parentStatements.iterator();

      YangParentStatementInfo parent;
      do {
         if (!var2.hasNext()) {
            return this.parentStatements.add(parentStatementInfo);
         }

         parent = (YangParentStatementInfo)var2.next();
      } while(parent.getParentYangKeyword() != parentStatementInfo.getParentYangKeyword());

      return false;
   }

   public List<YangParentStatementInfo> getParentStatements() {
      return this.parentStatements;
   }

   public YangParentStatementInfo getParentStatement(QName yangKeyword) {
      Iterator var2 = this.parentStatements.iterator();

      YangParentStatementInfo parentStatementInfo;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         parentStatementInfo = (YangParentStatementInfo)var2.next();
      } while(!parentStatementInfo.getParentYangKeyword().equals(yangKeyword));

      return parentStatementInfo;
   }
}
