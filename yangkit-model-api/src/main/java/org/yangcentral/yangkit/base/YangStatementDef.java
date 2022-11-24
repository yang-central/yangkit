package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * yang statement definition, include keyword,argument,is-yinelement,and cardinalities of sub statements
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class YangStatementDef {
   private QName keyword;
   private String argument;
   private boolean yinElement;
   private Map<QName, YangSubStatementInfo> subStatementInfos;

   public YangStatementDef(QName keyword, String argument, Map<QName, YangSubStatementInfo> subStatementInfos) {
      this(keyword, argument);
      this.subStatementInfos = subStatementInfos;
   }

   public YangStatementDef(QName keyword, String argument, boolean yinElement, Map<QName, YangSubStatementInfo> subStatementInfos) {
      this(keyword, argument, yinElement);
      this.subStatementInfos = subStatementInfos;
   }

   public YangStatementDef(QName keyword, String argument) {
      this.subStatementInfos = new ConcurrentHashMap();
      this.keyword = keyword;
      this.argument = argument;
      this.yinElement = false;
   }

   public YangStatementDef(QName keyword, String argument, boolean yinElement) {
      this.subStatementInfos = new ConcurrentHashMap();
      this.keyword = keyword;
      this.argument = argument;
      this.yinElement = yinElement;
   }
   /**
    * add sub statement info including keyword and cardinality
    * @param subStatementInfo the information of sub statement
    * @version 1.1.0
    * @throws
    * @return boolean
    * @author frank feng
    * @since 7/8/2022
    */
   public boolean addSubStatementInfo(YangSubStatementInfo subStatementInfo) {
      if(subStatementInfo == null){
         return false;
      }
      if(subStatementInfos.containsKey(subStatementInfo.getKeyword())){
         return false;
      }
      subStatementInfos.put(subStatementInfo.getKeyword(), subStatementInfo);
      return true;
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

   public Map<QName, YangSubStatementInfo> getSubStatementInfos() {
      return this.subStatementInfos;
   }
/**
 * get sub-statement information according keyword
 * @param subStatement qualified name of sub statement's keyword
 * @version 1.0.0
 * @throws
 * @return org.yangcentral.yangkit.base.YangSubStatementInfo
 * @author frank feng
 * @since 7/8/2022
 */
   public YangSubStatementInfo getSubStatementInfo(QName subStatement) {
      return subStatementInfos.get(subStatement);
   }
}
