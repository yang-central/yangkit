package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jaxen.Context;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.VariableContext;
import org.jaxen.expr.Expr;
import org.jaxen.expr.XPathExpr;
import org.jaxen.function.BooleanFunction;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.StringFunction;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.XPathSyntaxException;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.jaxen.util.SingletonList;
import org.yangcentral.yangkit.xpath.YangContextSupport;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.YangXPathVisitorContext;

public class YangXPathImpl implements YangXPath {
   private String yangVersion = "1";
   private final String exprText;
   private final XPathExpr xpath;
   private YangContextSupport support;
   private Navigator navigator;
   private YangXPathContext xPathContext;

   public YangXPathImpl(String xpathExpr) throws JaxenException {
      try {
         XPathReader reader = XPathReaderFactory.createReader();
         JaxenHandler handler = new JaxenHandler();
         handler.setXPathFactory(new YangXPathFactory());
         reader.setXPathHandler(handler);
         reader.parse(xpathExpr);
         this.xpath = handler.getXPathExpr();
      } catch (XPathSyntaxException var4) {
         throw new org.jaxen.XPathSyntaxException(var4);
      } catch (SAXPathException var5) {
         throw new JaxenException(var5);
      }

      this.exprText = xpathExpr;
      this.navigator = YangDocumentNavigator.getInstance();
   }

   public YangXPathContext getXPathContext() {
      return this.xPathContext;
   }

   public void setXPathContext(YangXPathVisitorContext xPathContext) {
      this.xPathContext = (YangXPathContext)xPathContext;
   }

   public Object evaluate(Object o) throws JaxenException {
      return this.evaluate(o, YangContextSupport.EvaluateType.NORMAL);
   }

   public Object evaluate(Object o, YangContextSupport.EvaluateType evaluateType) throws JaxenException {
      this.getContextSupport().setEvaluateType(evaluateType);
      this.getContextSupport().setContextData(o);
      List answer = this.selectNodes(o);
      if (answer != null && answer.size() == 1) {
         Object first = answer.get(0);
         if (first instanceof String || first instanceof Number || first instanceof Boolean) {
            return first;
         }
      }

      return answer;
   }

   public String valueOf(Object node) throws JaxenException {
      return this.stringValueOf(node);
   }

   public String stringValueOf(Object o) throws JaxenException {
      Context context = this.getContext(o);
      Object result = this.selectSingleNodeForContext(context);
      return result == null ? "" : StringFunction.evaluate(result, context.getNavigator());
   }

   public boolean booleanValueOf(Object o) throws JaxenException {
      Context context = this.getContext(o);
      List result = this.selectNodesForContext(context);
      return result == null ? false : BooleanFunction.evaluate(result, context.getNavigator());
   }

   public Number numberValueOf(Object o) throws JaxenException {
      Context context = this.getContext(o);
      Object result = this.selectSingleNodeForContext(context);
      return NumberFunction.evaluate(result, context.getNavigator());
   }

   public List selectNodes(Object o) throws JaxenException {
      YangContext context = this.getContext(o);
      return this.selectNodesForContext(context);
   }

   public Object selectSingleNode(Object o) throws JaxenException {
      List results = this.selectNodes(o);
      return results.isEmpty() ? null : results.get(0);
   }

   public void addNamespace(String prefix, String uri) throws JaxenException {
      NamespaceContext nsContext = this.getNamespaceContext();
      if (nsContext instanceof SimpleNamespaceContext) {
         ((SimpleNamespaceContext)nsContext).addNamespace(prefix, uri);
      } else {
         throw new JaxenException("Operation not permitted while using a non-simple namespace context.");
      }
   }

   public void setNamespaceContext(NamespaceContext namespaceContext) {
      this.getContextSupport().setNamespaceContext(namespaceContext);
   }

   public void setFunctionContext(FunctionContext functionContext) {
      this.getContextSupport().setFunctionContext(functionContext);
   }

   public void setVariableContext(VariableContext variableContext) {
      this.getContextSupport().setVariableContext(variableContext);
   }

   public NamespaceContext getNamespaceContext() {
      return this.getContextSupport().getNamespaceContext();
   }

   public FunctionContext getFunctionContext() {
      return this.getContextSupport().getFunctionContext();
   }

   public VariableContext getVariableContext() {
      return this.getContextSupport().getVariableContext();
   }

   public Expr getRootExpr() {
      return this.xpath.getRootExpr();
   }

   public String toString() {
      return this.exprText;
   }

   public String debug() {
      return this.xpath.toString();
   }

   protected YangContext getContext(Object node) {
      if (node instanceof YangContext) {
         return (YangContext)node;
      } else {
         YangContext fullContext = new YangContext(this.getContextSupport());
         if (node instanceof List) {
            fullContext.setNodeSet((List)node);
         } else {
            List list = new SingletonList(node);
            fullContext.setNodeSet(list);
         }

         return fullContext;
      }
   }

   public YangContextSupport getContextSupport() {
      if (this.support == null) {
         this.support = new YangContextSupport(this.createNamespaceContext(), this.createFunctionContext(), this.createVariableContext(), this.getNavigator());
      }

      return this.support;
   }

   public Navigator getNavigator() {
      return this.navigator;
   }

   protected FunctionContext createFunctionContext() {
      if (this.xPathContext == null) {
         return YangFunctionContext.getInstance("1");
      } else {
         String yangVersion = this.xPathContext.getYangContext().getCurModule().getEffectiveYangVersion();
         return YangFunctionContext.getInstance(yangVersion);
      }
   }

   protected NamespaceContext createNamespaceContext() {
      SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
      if (this.xPathContext == null) {
         return namespaceContext;
      } else {
         Iterator<Map.Entry<String, ModuleId>> prefixCache = this.xPathContext.getYangContext().getCurModule().getPrefixes().entrySet().iterator();

         while(prefixCache.hasNext()) {
            Map.Entry<String, ModuleId> entry = (Map.Entry)prefixCache.next();
            ModuleId moduleId = (ModuleId)entry.getValue();
            Optional<Module> module = this.xPathContext.getYangContext().getSchemaContext().getModule(moduleId);
            if (module.isPresent()) {
               namespaceContext.addNamespace((String)entry.getKey(), ((Module)module.get()).getMainModule().getNamespace().getUri().toString());
            }
         }

         return namespaceContext;
      }
   }

   protected VariableContext createVariableContext() {
      return new SimpleVariableContext();
   }

   protected List selectNodesForContext(Context context) throws JaxenException {
      List list = this.xpath.asList(context);
      return list;
   }

   protected Object selectSingleNodeForContext(Context context) throws JaxenException {
      List results = this.selectNodesForContext(context);
      return results.isEmpty() ? null : results.get(0);
   }
}
