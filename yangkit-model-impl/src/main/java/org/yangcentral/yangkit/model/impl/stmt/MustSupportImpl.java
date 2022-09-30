package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilderFactory;
import org.yangcentral.yangkit.model.api.stmt.Must;
import org.yangcentral.yangkit.model.api.stmt.MustSupport;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import org.yangcentral.yangkit.xpath.impl.YangXPathContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathValidator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MustSupportImpl implements MustSupport {
   private List<Must> musts = new ArrayList();
   private Object contextNode;
   private SchemaNode self;

   public Must getMust(int index) {
      return (Must)this.musts.get(index);
   }

   public Must getMust(String condition) {
      Iterator mustIterator = this.musts.iterator();

      Must must;
      do {
         if (!mustIterator.hasNext()) {
            return null;
         }

         must = (Must)mustIterator.next();
      } while(!must.getArgStr().equals(condition));

      return must;
   }

   public List<Must> getMusts() {
      return Collections.unmodifiableList(this.musts);
   }

   public void setMusts(List<Must> musts) {
      this.musts = musts;
   }

   public ValidatorResult addMust(Must must) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Must orig = this.getMust(must.getArgStr());
      if (null != orig) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, must, Severity.WARNING));
         return validatorResultBuilder.build();
      } else {
         this.musts.add(must);
         return validatorResultBuilder.build();
      }
   }

   public void removeMust(String condition) {
      int size = this.musts.size();
      int index = -1;

      for(int i = 0; i < size; ++i) {
         Must must = (Must)this.musts.get(i);
         if (must != null && must.getArgStr().equals(condition)) {
            index = i;
            break;
         }
      }

      if (index != -1) {
         this.musts.remove(index);
      }

   }
   /**
    * remove all musts
    * @version 1.0.0
    * @throws
    * @return void
    * @author frank feng
    * @since 7/21/2022
    */
   public void removeMusts(){
      this.musts.clear();
   }
   public ValidatorResult updateMust(Must must) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      int size = this.musts.size();
      int index = -1;

      for(int i = 0; i < size; ++i) {
         Must m = (Must)this.musts.get(i);
         if (m != null && m.getArgStr().equals(must.getArgStr())) {
            index = i;
            break;
         }
      }

      if (index != -1) {
         this.musts.set(index, must);
      } else {
         validatorResultBuilder.addRecord(ModelUtil.reportError(must,
                 ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=must"})));
      }

      return validatorResultBuilder.build();
   }

   public Object getContextNode() {
      return this.contextNode;
   }

   public void setContextNode(Object contextNode) {
      this.contextNode = contextNode;
   }

   public SchemaNode getSelf() {
      return this.self;
   }

   public void setSelf(SchemaNode self) {
      this.self = self;
   }

   public ValidatorResult validateMusts() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator<Must> mustIterator = this.musts.iterator();

      while(mustIterator.hasNext()) {
         Must must = mustIterator.next();
         YangXPath xpath = must.getXPathExpression();
         Object contextNode = XPathUtil.getXPathContextNode(self);
         YangXPathContext yangXPathContext = new YangXPathContext(must.getContext(), contextNode, this.self);
         xpath.setXPathContext(yangXPathContext);
         YangXPathValidator yangXPathValidator = new YangXPathValidator(xpath, yangXPathContext, new ValidatorResultBuilderFactory());
         validatorResultBuilder.merge(yangXPathValidator.visit(xpath.getRootExpr(), contextNode));
      }

      return validatorResultBuilder.build();
   }
}
