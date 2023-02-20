package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.common.api.Builder;
import org.yangcentral.yangkit.common.api.BuilderFactory;
import java.util.List;
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
import org.jaxen.expr.Predicate;
import org.jaxen.expr.RelationalExpr;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.UnionExpr;
import org.jaxen.expr.VariableReferenceExpr;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.YangXPathVisitor;
import org.yangcentral.yangkit.xpath.YangXPathVisitorContext;

public abstract class YangXPathBaseVisitor<T, C, Context extends YangXPathVisitorContext> implements YangXPathVisitor<T, C> {
   private YangXPath yangXPath;
   private Context context;
   private BuilderFactory<T> builderFactory;

   public YangXPathBaseVisitor(YangXPath yangXPath, Context context, BuilderFactory<T> builderFactory) {
      this.yangXPath = yangXPath;
      this.context = context;
      this.builderFactory = builderFactory;
   }

   public YangXPath getYangXPath() {
      return this.yangXPath;
   }

   public Context getContext() {
      return this.context;
   }

   public BuilderFactory<T> getBuilderFactory() {
      return this.builderFactory;
   }

   public T visit(Expr expr, C context) {
      if (expr instanceof BinaryExpr) {
         return this.visitBinaryExpr((BinaryExpr)expr, context);
      } else if (expr instanceof FilterExpr) {
         return this.visitFilterExpr((FilterExpr)expr, context);
      } else if (expr instanceof FunctionCallExpr) {
         return this.visitFunctionCallExpr((FunctionCallExpr)expr, context);
      } else if (expr instanceof LiteralExpr) {
         return this.visitLiteralExpr((LiteralExpr)expr, context);
      } else if (expr instanceof LocationPath) {
         return this.visitLocationExpr((LocationPath)expr, context);
      } else if (expr instanceof NumberExpr) {
         return this.visitNumberExpr((NumberExpr)expr, context);
      } else if (expr instanceof PathExpr) {
         return this.visitPathExpr((PathExpr)expr, context);
      } else if (expr instanceof UnaryExpr) {
         return this.visitUnaryExpr((UnaryExpr)expr, context);
      } else if (expr instanceof VariableReferenceExpr) {
         return this.visitVariableReferenceExpr((VariableReferenceExpr)expr, context);
      } else {
         throw new IllegalArgumentException("unrecognized expr type.");
      }
   }

   public T visitAdditiveExpr(AdditiveExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitBinaryExpr(BinaryExpr expr, C context) {
      T left = this.visit(expr.getLHS(), context);
      T right = this.visit(expr.getRHS(), context);
      Builder<T> builder = this.builderFactory.getBuilder();
      builder.merge(left);
      builder.merge(right);
      if (expr instanceof AdditiveExpr) {
         builder.merge(this.visitAdditiveExpr((AdditiveExpr)expr, context));
      } else if (expr instanceof EqualityExpr) {
         builder.merge(this.visitEqualityExpr((EqualityExpr)expr, context));
      } else if (expr instanceof LogicalExpr) {
         builder.merge(this.visitLogicalExpr((LogicalExpr)expr, context));
      } else if (expr instanceof MultiplicativeExpr) {
         builder.merge(this.visitMultiplicativeExpr((MultiplicativeExpr)expr, context));
      } else if (expr instanceof RelationalExpr) {
         builder.merge(this.visitRelationalExpr((RelationalExpr)expr, context));
      } else {
         if (!(expr instanceof UnionExpr)) {
            throw new IllegalArgumentException("unrecognized expr type.");
         }

         builder.merge(this.visitUnionExpr((UnionExpr)expr, context));
      }

      return builder.build();
   }

   public T visitEqualityExpr(EqualityExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitFilterExpr(FilterExpr expr, C context) {
      Expr otherExpr = expr.getExpr();
      Builder<T> builder = this.builderFactory.getBuilder();
      builder.merge(this.visit(otherExpr, context));
      List predicates = expr.getPredicates();

      for (Object o : predicates) {
         Predicate predicate = (Predicate) o;
         builder.merge(this.visit(predicate.getExpr(), context));
      }

      return builder.build();
   }

   public T visitFunctionCallExpr(FunctionCallExpr expr, C context) {
      Builder<T> builder = this.builderFactory.getBuilder();
      List parameters = expr.getParameters();

      for (Object o : parameters) {
         Expr para = (Expr) o;
         builder.merge(this.visit(para, context));
      }

      return builder.build();
   }

   public T visitLiteralExpr(LiteralExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitLocationExpr(LocationPath expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitLogicalExpr(LogicalExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitMultiplicativeExpr(MultiplicativeExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitNumberExpr(NumberExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitPathExpr(PathExpr expr, C context) {
      Builder<T> builder = this.builderFactory.getBuilder();
      builder.merge(this.visit(expr.getFilterExpr(), context));
      builder.merge(this.visit(expr.getLocationPath(), context));
      return builder.build();
   }

   public T visitRelationalExpr(RelationalExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitUnaryExpr(UnaryExpr expr, C context) {
      Builder<T> builder = this.builderFactory.getBuilder();
      builder.merge(this.visit(expr.getExpr(), context));
      return builder.build();
   }

   public T visitUnionExpr(UnionExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }

   public T visitVariableReferenceExpr(VariableReferenceExpr expr, C context) {
      return this.builderFactory.getBuilder().build();
   }
}
