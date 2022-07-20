package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.TypedefContainer;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;

class TypedefContainerImpl implements TypedefContainer {
   private List<Typedef> typedefs = new ArrayList();
   private YangContext yangContext;

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public List<Typedef> getTypedefs() {
      return this.typedefs;
   }

   public Typedef getTypedef(int index) {
      return (Typedef)this.typedefs.get(index);
   }

   public ValidatorResult addTypedef(Typedef typedef) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Typedef orig = this.getTypedef(typedef.getArgStr());
      if (null != orig) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, typedef));
         typedef.setErrorStatement(true);
         return validatorResultBuilder.build();
      } else {
         this.typedefs.add(typedef);
         this.yangContext.getTypedefIdentifierCache().put(typedef.getArgStr(), typedef);
         return validatorResultBuilder.build();
      }
   }

   public Typedef getTypedef(String defName) {
      return this.yangContext.getTypedef(defName);
   }

   public Typedef removeTypedef(String name) {
      Typedef typedef = getTypedef(name);
      if(typedef == null){
         return null;
      }
      this.typedefs.remove(typedef);
      this.yangContext.getTypedefIdentifierCache().remove(typedef.getArgStr());
      return typedef;
   }


   public void removeTypedefs() {
      for(Typedef typedef:typedefs){
         getYangContext().getTypedefIdentifierCache().remove(typedef.getArgStr());
      }
      typedefs.clear();
   }
}
