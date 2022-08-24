package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.DataDefContainer;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Uses;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataDefContainerImpl implements DataDefContainer {
   private List<DataDefinition> dataDefs = new ArrayList();
   private YangContext yangContext;

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public List<DataDefinition> getDataDefChildren() {
      return Collections.unmodifiableList(this.dataDefs);
   }

   public DataDefinition getDataDefChild(String name) {
      SchemaNode schemaNode = this.getYangContext().getSchemaNodeIdentifierCache().get(name);
      if (null == schemaNode) {
         return null;
      } else {
         return schemaNode instanceof DataDefinition ? (DataDefinition)schemaNode : null;
      }
   }

   public DataDefinition removeDataDefChild(String name){
      DataDefinition dataDefinition = (DataDefinition) this.getYangContext().getSchemaNodeIdentifierCache().remove(name);
      if(null == dataDefinition){
         return null;
      }
      this.dataDefs.remove(dataDefinition);
      return dataDefinition;
   }

   public void removeDataDefs(){
      for(DataDefinition dataDefinition:dataDefs){
         this.getYangContext().getSchemaNodeIdentifierCache().remove(dataDefinition.getArgStr());
      }
      dataDefs.clear();
   }
   public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(dataDefinition instanceof Uses)) {
         DataDefinition orig = (DataDefinition) this.getYangContext().getSchemaNodeIdentifierCache().get(dataDefinition.getArgStr());
         if (null != orig) {
            validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, dataDefinition));
            dataDefinition.setErrorStatement(true);
            return validatorResultBuilder.build();
         }

         this.getYangContext().getSchemaNodeIdentifierCache().put(dataDefinition.getArgStr(), dataDefinition);
      }

      this.dataDefs.add(dataDefinition);
      return validatorResultBuilder.build();
   }
}
