package org.yangcentral.yangkit.xpath;

import org.jaxen.ContextSupport;
import org.jaxen.FunctionContext;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.VariableContext;

public class YangContextSupport extends ContextSupport {
   private Object contextData;
   private EvaluateType evaluateType;

   public YangContextSupport(NamespaceContext namespaceContext, FunctionContext functionContext, VariableContext variableContext, Navigator navigator) {
      super(namespaceContext, functionContext, variableContext, navigator);
      this.evaluateType = EvaluateType.NORMAL;
   }

   public Object getContextData() {
      return this.contextData;
   }

   public void setContextData(Object contextData) {
      this.contextData = contextData;
   }

   public EvaluateType getEvaluateType() {
      return this.evaluateType;
   }

   public void setEvaluateType(EvaluateType evaluateType) {
      this.evaluateType = evaluateType;
   }

   public static enum EvaluateType {
      NORMAL,
      NEST,
   }
}
