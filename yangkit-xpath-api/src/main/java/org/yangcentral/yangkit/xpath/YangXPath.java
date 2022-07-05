package org.yangcentral.yangkit.xpath;

import java.io.Serializable;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.expr.Expr;

public interface YangXPath extends XPath, Serializable {
   Expr getRootExpr();

   YangXPathVisitorContext getXPathContext();

   void setXPathContext(YangXPathVisitorContext var1);

   Object evaluate(Object var1, YangContextSupport.EvaluateType var2) throws JaxenException;
}
