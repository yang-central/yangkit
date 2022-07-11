package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.YangSpecification;
import org.yangcentral.yangkit.base.YangStatementDef;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YangUnknownRegister {
   private static final YangUnknownRegister ourInstance = new YangUnknownRegister();
   private Map<QName, YangUnknownParserPolicy> unknownInfos = new ConcurrentHashMap();

   public static YangUnknownRegister getInstance() {
      return ourInstance;
   }

   private YangUnknownRegister() {
   }

   public synchronized boolean register(QName keyword, Class<? extends YangUnknown> clazz) {
      YangUnknownParserPolicy yangUnknownParserPolicy = new YangUnknownParserPolicy(keyword, clazz);
      return this.register(yangUnknownParserPolicy);
   }

   public synchronized boolean register(YangUnknownParserPolicy yangUnknownParserPolicy) {
      if (this.unknownInfos.containsKey(yangUnknownParserPolicy.getKeyword())) {
         return false;
      } else {
         this.unknownInfos.put(yangUnknownParserPolicy.getKeyword(), yangUnknownParserPolicy);
         if (yangUnknownParserPolicy.getParentStatements().size() != 0) {
            Iterator parentStatementInfoIterator = yangUnknownParserPolicy.getParentStatements().iterator();

            while(parentStatementInfoIterator.hasNext()) {
               YangParentStatementInfo parentStatementInfo = (YangParentStatementInfo)parentStatementInfoIterator.next();
               YangStatementDef parentStatementDefVer11 = YangSpecification.getVersion11Spec().getStatementDef(parentStatementInfo.getParentYangKeyword());
               if (parentStatementDefVer11 != null) {
                  parentStatementDefVer11.addSubStatementInfo(yangUnknownParserPolicy.getKeyword(), parentStatementInfo.getCardinality());
               }

               YangStatementDef parentStatementDefVer1 = YangSpecification.getVersion1Spec().getStatementDef(parentStatementInfo.getParentYangKeyword());
               if (parentStatementDefVer1 != null) {
                  parentStatementDefVer1.addSubStatementInfo(yangUnknownParserPolicy.getKeyword(), parentStatementInfo.getCardinality());
               }
            }
         }

         return true;
      }
   }

   public synchronized void unRegister(QName keyword) {
      YangUnknownParserPolicy unknownParserPolicy = this.unknownInfos.remove(keyword);
      if (unknownParserPolicy.getParentStatements().size() != 0) {
         Iterator parentStatementInfoIterator = unknownParserPolicy.getParentStatements().iterator();

         while(parentStatementInfoIterator.hasNext()) {
            YangParentStatementInfo parentStatementInfo = (YangParentStatementInfo)parentStatementInfoIterator.next();
            YangSpecification specification11 = YangSpecification.getVersion11Spec();
            YangStatementDef parentStatementDef11 = specification11.getStatementDef(parentStatementInfo.getParentYangKeyword());
            if (null != parentStatementDef11) {
               parentStatementDef11.removeSubStatementInfo(keyword);
            }

            YangSpecification specification1 = YangSpecification.getVersion1Spec();
            YangStatementDef parentStatementDef1 = specification1.getStatementDef(parentStatementInfo.getParentYangKeyword());
            if (null != parentStatementDef1) {
               parentStatementDef1.removeSubStatementInfo(keyword);
            }
         }
      }

   }

   public Collection<YangUnknownParserPolicy> getUnknownInfos() {
      return this.unknownInfos.values();
   }

   public YangUnknownParserPolicy getUnknownInfo(QName keyword) {
      return this.unknownInfos.get(keyword);
   }
}
