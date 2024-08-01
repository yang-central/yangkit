package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.expr.*;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangXPath;

import java.net.URI;
import java.util.List;

public class XPathUtil {
   public static Object getXPathContextNode(SchemaNode current) {
      if (current == null) {
         return null;
      }
      if(current instanceof TreeNode){
         return current;
      }

      SchemaNodeContainer closestAncestorNode = current.getClosestAncestorNode();
      return closestAncestorNode instanceof Module ? new YangXPathRoot((Module)closestAncestorNode) : closestAncestorNode;
   }

   public static Identity getIdentity(YangSchemaContext schemaContext, String uri, String identityName) {
      List<Module> contextModules = schemaContext.getModule(URI.create(uri));
      if (contextModules.isEmpty()) {
         return null;
      } else {
         Module curModule = (Module)contextModules.get(0);
         Identity destIdentity = curModule.getIdentity(identityName);
         return destIdentity;
      }
   }

   public static YangSchemaContext getSchemaContext(Object context) {
      if (context instanceof YangData) {
         YangData yangData = (YangData)context;
         return yangData.getSchemaNode().getContext().getSchemaContext();
      } else if (context instanceof YangDataDocument) {
         YangDataDocument yangDataDocument = (YangDataDocument)context;
         return yangDataDocument.getSchemaContext();
      } else {
         throw new IllegalArgumentException("error type");
      }
   }
   private static boolean binaryEquals(BinaryExpr left,BinaryExpr right,
                                       YangXPath leftXPath,YangXPath rightXPath){
      if(!left.getOperator().equals(right.getOperator())){
         return false;
      }
      if(!equals(left.getLHS(),right.getLHS(),leftXPath,rightXPath)){
         return false;
      }
      if(!equals(left.getRHS(),right.getRHS(),leftXPath,rightXPath)){
         return false;
      }
      return true;
   }
   private static boolean filterEquals(FilterExpr left,FilterExpr right,
                                       YangXPath leftXPath,YangXPath rightXPath){
      if(!equals(left.getExpr(),right.getExpr(),leftXPath,rightXPath)){
         return false;
      }
      List leftPredicates = left.getPredicates();
      List rightPredicates = right.getPredicates();
      if(leftPredicates.size() != rightPredicates.size()){
         return false;
      }
      for(int i = 0; i< leftPredicates.size();i++){
         Predicate leftPredicate= (Predicate) leftPredicates.get(i);
         Predicate rightPredicate = (Predicate) rightPredicates.get(i);
         if(!equals(leftPredicate.getExpr(),rightPredicate.getExpr(),leftXPath,rightXPath)){
            return false;
         }
      }
      return true;
   }
   private static boolean functionCallEquals(FunctionCallExpr left,FunctionCallExpr right,
                                             YangXPath leftXPath,YangXPath rightXPath){
      if(!left.getFunctionName().equals(right.getFunctionName())){
         return false;
      }
      if(!left.getPrefix().equals(right.getPrefix())){
         return false;
      }
      List leftParameters = left.getParameters();
      List rightParameters = right.getParameters();
      if(leftParameters.size() != rightParameters.size()){
         return false;
      }
      for(int i =0;i < leftParameters.size();i++){
         Expr leftPara = (Expr) leftParameters.get(i);
         Expr rightPara = (Expr) rightParameters.get(i);
         if(!equals(leftPara,rightPara,leftXPath,rightXPath)){
            return false;
         }
      }
      return true;
   }

   private static URI getNamespace(YangXPathContext yangXPathContext,String prefix){
      URI namespace = null;
      if(prefix == null || prefix.length() == 0){
         Object contextNode = yangXPathContext.getContextNode();
         if (contextNode instanceof Module) {
            Module curModule = (Module)contextNode;
            namespace = curModule.getMainModule().getNamespace().getUri();
         } else {
            namespace = ((SchemaNode)contextNode).getIdentifier().getNamespace();
         }
         return namespace;
      }
      Module module = null;
      try {
         module = ModelUtil.findModuleByPrefix(yangXPathContext.getYangContext(),prefix);
      } catch (ModelException e) {
         module = null;
      }
      namespace = module.getMainModule().getNamespace().getUri();
      return namespace;
   }
   private static boolean literEquals(LiteralExpr left,LiteralExpr right,YangXPath leftXPath,
                                      YangXPath rightXPath){
      FName leftFName = new FName(left.getLiteral());
      FName rightFName = new FName(right.getLiteral());
      if((leftFName.getPrefix() == null) && (rightFName.getPrefix() == null)){
         return leftFName.getLocalName().equals(rightFName.getLocalName());
      }
      if(leftFName.getPrefix() == null || rightFName.getPrefix() == null){
         return false;
      }
      YangXPathContext leftXPathContext = (YangXPathContext) leftXPath.getXPathContext();
      YangXPathContext rightXPathContext = (YangXPathContext) rightXPath.getXPathContext();

      URI leftNamespace = getNamespace(leftXPathContext, leftFName.getPrefix());
      URI rightNamespace = getNamespace(rightXPathContext, rightFName.getPrefix());
      if(!leftNamespace.equals(rightNamespace)){
           return false;
      }

      return leftFName.getLocalName().equals(rightFName.getLocalName());
   }

   private static boolean stepEquals(Step left,Step right,YangXPath leftXPath,
                                     YangXPath rightXPath){
      if(left.getAxis() != right.getAxis()){
         return false;
      }
      if(left instanceof NameStep){
         NameStep leftNameStep = (NameStep) left;
         NameStep rightNameStep = (NameStep) right;
         URI leftNamespace = getNamespace((YangXPathContext) leftXPath.getXPathContext(), leftNameStep.getPrefix());
         URI rightNamespace = getNamespace((YangXPathContext) rightXPath.getXPathContext(), rightNameStep.getPrefix());

         if(!leftNamespace.equals(rightNamespace)){
            return false;
         }
         if(!leftNameStep.getLocalName().equals(rightNameStep.getLocalName())){
            return false;
         }
      } else {
         if(!left.getText().equals(right.getText())){
            return false;
         }
      }

      List leftPredicates = left.getPredicates();
      List rightPredicates = right.getPredicates();
      if(leftPredicates.size() != rightPredicates.size()){
         return false;
      }
      for(int i = 0; i< leftPredicates.size();i++){
         Predicate leftPredicate= (Predicate) leftPredicates.get(i);
         Predicate rightPredicate = (Predicate) rightPredicates.get(i);
         if(!equals(leftPredicate.getExpr(),rightPredicate.getExpr(),leftXPath,rightXPath)){
            return false;
         }
      }
      return true;
   }
   private static boolean locationPathEquals(LocationPath left, LocationPath right,
                                             YangXPath leftXPath,
                                             YangXPath rightXPath){
      if(left.isAbsolute() != right.isAbsolute()){
         return false;
      }
      List leftSteps = left.getSteps();
      List rightSteps = right.getSteps();
      if(leftSteps.size() != rightSteps.size()){
         return false;
      }
      for(int i =0; i< leftSteps.size();i++){
         Step leftStep = (Step) leftSteps.get(i);
         Step rightStep = (Step) rightSteps.get(i);
         if(!stepEquals(leftStep,rightStep,leftXPath,rightXPath)){
            return false;
         }
      }
      return true;
   }
   private static boolean pathEquals(PathExpr left,PathExpr right,YangXPath leftXPath,
                                     YangXPath rightXPath){
      if(!equals(left.getLocationPath(),right.getLocationPath(),leftXPath,rightXPath)){
         return false;
      }
      if(!equals(left.getFilterExpr(),right.getFilterExpr(),leftXPath,rightXPath)){
         return false;
      }
      return true;
   }
   private static boolean variableReferenceEquals(VariableReferenceExpr left, VariableReferenceExpr right,
                                                  YangXPath leftXPath,YangXPath rightXPath){
      URI leftNamespace = getNamespace((YangXPathContext) leftXPath.getXPathContext(), left.getPrefix());
      URI rightNamespace = getNamespace((YangXPathContext) rightXPath.getXPathContext(), right.getPrefix());

      if(!leftNamespace.equals(rightNamespace)){
         return false;
      }
      return left.getVariableName().equals(right.getVariableName());
   }
   public static boolean equals(Expr left, Expr right,
                                YangXPath leftXPath, YangXPath rightXPath){
      if(left instanceof BinaryExpr){
         if(!(right instanceof BinaryExpr))
            return false;
         return binaryEquals((BinaryExpr) left,(BinaryExpr)right,leftXPath,rightXPath);
      } else if (left instanceof FilterExpr){
         if(!(right instanceof FilterExpr)){
            return false;
         }
         return filterEquals((FilterExpr) left, (FilterExpr) right,leftXPath,rightXPath);
      } else if (left instanceof FunctionCallExpr){
         if(!(right instanceof FunctionCallExpr)){
            return false;
         }
         return functionCallEquals((FunctionCallExpr) left, (FunctionCallExpr) right,leftXPath,rightXPath);
      } else if (left instanceof LiteralExpr){
         if(!(right instanceof LiteralExpr)){
            return false;
         }
         return literEquals((LiteralExpr) left, (LiteralExpr) right,leftXPath,rightXPath);
      } else if (left instanceof LocationPath){
         if(!(right instanceof LocationPath)){
            return false;
         }
         return locationPathEquals((LocationPath) left, (LocationPath) right,leftXPath,rightXPath);
      } else if (left instanceof NumberExpr){
         if(!(right instanceof NumberExpr)){
            return false;
         }
         return ((NumberExpr) left).getNumber().equals(((NumberExpr) right).getNumber());
      } else if (left instanceof PathExpr){
         if (!(right instanceof PathExpr)) {
            return false;
         }
         return pathEquals((PathExpr) left, (PathExpr) right,leftXPath,rightXPath);
      } else if (left instanceof UnaryExpr){
         if(!(right instanceof UnaryExpr)){
            return false;
         }
         return equals(((UnaryExpr) left).getExpr(),((UnaryExpr) right).getExpr(),leftXPath,rightXPath);
      } else if (left instanceof VariableReferenceExpr){
         if(!(right instanceof VariableReferenceExpr)){
            return false;
         }
         return variableReferenceEquals((VariableReferenceExpr) left, (VariableReferenceExpr) right,leftXPath,rightXPath);
      }
      throw new IllegalArgumentException("unrecognized expr type.");
   }
}
