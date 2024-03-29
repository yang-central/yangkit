package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.ActionContainer;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class ActionContainerImpl implements ActionContainer {
   private List<Action> actions = new ArrayList<>();
   private YangContext yangContext;

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public Action getAction(String actionName) {
      Iterator<Action> iterator = this.actions.iterator();

      Action action;
      do {
         if (!iterator.hasNext()) {
            return null;
         }

         action = iterator.next();
      } while(!action.getArgStr().equals(actionName));

      return action;
   }

   public Action removeAction(String actionName){
      Action action = getAction(actionName);
      this.getYangContext().getSchemaNodeIdentifierCache().remove(actionName);
      actions.remove(action);
      return action;
   }

   public List<Action> getActions() {
      return Collections.unmodifiableList(this.actions);
   }

   public void removeActions(){
      for(Action action :actions){
         this.getYangContext().getSchemaNodeIdentifierCache().remove(action.getArgStr());
      }
      actions.clear();
   }

   public ValidatorResult addAction(Action action) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      SchemaNode schemaNode = this.getYangContext().getSchemaNodeIdentifierCache().get(action.getArgStr());
      if (schemaNode != null) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(schemaNode, action));
         action.setErrorStatement(true);
         return validatorResultBuilder.build();
      } else {
         this.actions.add(action);
         this.getYangContext().getSchemaNodeIdentifierCache().put(action.getArgStr(), action);
         return validatorResultBuilder.build();
      }
   }
}
