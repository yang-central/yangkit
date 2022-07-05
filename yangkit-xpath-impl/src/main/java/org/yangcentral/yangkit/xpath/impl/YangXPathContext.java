package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.xpath.YangXPathVisitorContext;

public class YangXPathContext implements YangXPathVisitorContext<YangXPathContext> {
   private YangContext yangContext;
   private Object contextNode;
   private Object currentNode;
   private SchemaNode defineNode;

   public YangXPathContext(YangContext yangContext, Object contextNode, SchemaNode defineNode) {
      this.yangContext = yangContext;
      this.contextNode = contextNode;
      this.currentNode = contextNode;
      this.defineNode = defineNode;
   }

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public Object getContextNode() {
      return this.contextNode;
   }

   public SchemaNode getDefineNode() {
      return this.defineNode;
   }

   public Object getCurrentNode() {
      return this.currentNode;
   }

   public void setCurrentNode(Object currentNode) {
      this.currentNode = currentNode;
   }

   public YangXPathContext newContext() {
      YangXPathContext newContext = new YangXPathContext(this.yangContext, this.contextNode, this.defineNode);
      return newContext;
   }
}
