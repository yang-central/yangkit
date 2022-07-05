package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YangStatementDef {
   private QName keyword;
   private String argument;
   private boolean yinElement;
   private Map<QName, Cardinality> subStatementInfos;

   public YangStatementDef(QName keyword, String argument, Map<QName, Cardinality> subStatementInfos) {
      this(keyword, argument);
      this.subStatementInfos = subStatementInfos;
   }

   public YangStatementDef(QName keyword, String argument, boolean yinElement, Map<QName, Cardinality> subStatementInfos) {
      this(keyword, argument, yinElement);
      this.subStatementInfos = subStatementInfos;
   }

   public YangStatementDef(QName keyword, String argument) {
      this.yinElement = false;
      this.subStatementInfos = new ConcurrentHashMap();
      this.keyword = keyword;
      this.argument = argument;
      this.yinElement = false;
   }

   public YangStatementDef(QName keyword, String argument, boolean yinElement) {
      this.yinElement = false;
      this.subStatementInfos = new ConcurrentHashMap();
      this.keyword = keyword;
      this.argument = argument;
      this.yinElement = yinElement;
   }

   public boolean addSubStatementInfo(QName subStatement, Cardinality cardinality) {
      if (null != subStatement && cardinality != null) {
         if (null != this.getSubStatementCardinality(subStatement)) {
            return false;
         } else {
            this.subStatementInfos.put(subStatement, cardinality);
            return true;
         }
      } else {
         return false;
      }
   }

   public void removeSubStatementInfo(QName subStatement) {
      this.subStatementInfos.remove(subStatement);
   }

   public QName getKeyword() {
      return this.keyword;
   }

   public String getArgument() {
      return this.argument;
   }

   public boolean isYinElement() {
      return this.yinElement;
   }

   public Map<QName, Cardinality> getSubStatementInfos() {
      return this.subStatementInfos;
   }

   public Cardinality getSubStatementCardinality(QName subStatement) {
      Iterator<Map.Entry<QName, Cardinality>> it = this.subStatementInfos.entrySet().iterator();

      Map.Entry entry;
      do {
         if (!it.hasNext()) {
            return null;
         }

         entry = (Map.Entry)it.next();
      } while(!((QName)entry.getKey()).equals(subStatement));

      return (Cardinality)entry.getValue();
   }
}
