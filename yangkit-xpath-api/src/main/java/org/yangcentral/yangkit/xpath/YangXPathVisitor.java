package org.yangcentral.yangkit.xpath;

import org.jaxen.expr.AdditiveExpr;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.LogicalExpr;
import org.jaxen.expr.MultiplicativeExpr;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.RelationalExpr;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.UnionExpr;
import org.jaxen.expr.VariableReferenceExpr;

public interface YangXPathVisitor<T, C> {
   T visitAdditiveExpr(AdditiveExpr var1, C var2);

   T visitBinaryExpr(BinaryExpr var1, C var2);

   T visitEqualityExpr(EqualityExpr var1, C var2);

   T visitFilterExpr(FilterExpr var1, C var2);

   T visitFunctionCallExpr(FunctionCallExpr var1, C var2);

   T visitLiteralExpr(LiteralExpr var1, C var2);

   T visitLocationExpr(LocationPath var1, C var2);

   T visitLogicalExpr(LogicalExpr var1, C var2);

   T visitMultiplicativeExpr(MultiplicativeExpr var1, C var2);

   T visitNumberExpr(NumberExpr var1, C var2);

   T visitPathExpr(PathExpr var1, C var2);

   T visitRelationalExpr(RelationalExpr var1, C var2);

   T visitUnaryExpr(UnaryExpr var1, C var2);

   T visitUnionExpr(UnionExpr var1, C var2);

   T visitVariableReferenceExpr(VariableReferenceExpr var1, C var2);

   T visit(Expr var1, C var2);
}
