package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupingImpl extends EntityImpl implements Grouping {
   private ActionContainerImpl actionContainer = new ActionContainerImpl();
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
   private NotificationContainerImpl notificationContainer = new NotificationContainerImpl();
   private List<YangStatement> referencedBys = new ArrayList<>();
   private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();

   public void setContext(YangContext context) {
      super.setContext(context);
      this.actionContainer.setYangContext(context);
      this.dataDefContainer.setYangContext(context);
      this.groupingDefContainer.setYangContext(context);
      this.notificationContainer.setYangContext(context);
      this.typedefContainer.setYangContext(context);
   }

   public GroupingImpl(String argStr) {
      super(argStr);
   }

   public Action getAction(String actionName) {
      return this.actionContainer.getAction(actionName);
   }

   public List<Action> getActions() {
      return this.actionContainer.getActions();
   }

   public ValidatorResult addAction(Action action) {
      return this.actionContainer.addAction(action);
   }

   public List<DataDefinition> getDataDefChildren() {
      return this.dataDefContainer.getDataDefChildren();
   }

   public DataDefinition getDataDefChild(String name) {
      return this.dataDefContainer.getDataDefChild(name);
   }

   public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
      return this.dataDefContainer.addDataDefChild(dataDefinition);
   }

   public List<Grouping> getGroupings() {
      return this.groupingDefContainer.getGroupings();
   }

   public Grouping getGrouping(String name) {
      return this.groupingDefContainer.getGrouping(name);
   }

   public List<Notification> getNotifications() {
      return this.notificationContainer.getNotifications();
   }

   public Notification getNotification(String name) {
      return this.notificationContainer.getNotification(name);
   }

   public ValidatorResult addNotification(Notification notification) {
      return this.notificationContainer.addNotification(notification);
   }

   public List<Typedef> getTypedefs() {
      return this.typedefContainer.getTypedefs();
   }

   public Typedef getTypedef(int index) {
      return this.typedefContainer.getTypedef(index);
   }

   public Typedef getTypedef(String defName) {
      return this.typedefContainer.getTypedef(defName);
   }

   @Override
   public boolean checkChild(YangStatement subStatement) {
      boolean result = super.checkChild(subStatement);
      if(!result){
         return false;
      }
      YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
      switch (builtinKeyword){
         case TYPEDEF:{
            if(getTypedef(subStatement.getArgStr()) != null){
               return false;
            }
            return true;
         }
         case GROUPING:{
            if(getGrouping(subStatement.getArgStr()) != null){
               return false;
            }
            return true;
         }
         case CONTAINER:
         case LIST:
         case LEAF:
         case LEAFLIST:
         case ANYDATA:
         case ANYXML:
         case CHOICE:
         case ACTION:
         case NOTIFICATION:{
            if(getContext().getSchemaNodeIdentifierCache().containsKey(subStatement.getArgStr())){
               return false;
            }
            return true;
         }
         default:{
            return true;
         }
      }
   }

   @Override
   protected void clearSelf() {
      this.typedefContainer.removeTypedefs();
      this.groupingDefContainer.removeGroupings();
      this.dataDefContainer.removeDataDefs();
      this.actionContainer.removeActions();
      this.notificationContainer.removeNotifications();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator elementIterator = subElements.iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
            switch (builtinKeyword) {
               case TYPEDEF:
                  Typedef newTypedef = (Typedef)builtinStatement;
                  validatorResultBuilder.merge(this.typedefContainer.addTypedef(newTypedef));
                  break;
               case GROUPING:
                  Grouping newGrouping = (Grouping)builtinStatement;
                  validatorResultBuilder.merge(this.groupingDefContainer.addGrouping(newGrouping));
                  break;
               case CONTAINER:
               case LIST:
               case LEAF:
               case LEAFLIST:
               case ANYDATA:
               case ANYXML:
               case CHOICE:
               case USES:
                  DataDefinition newDataDefinition = (DataDefinition)builtinStatement;
                  validatorResultBuilder.merge(this.dataDefContainer.addDataDefChild(newDataDefinition));
                  break;
               case ACTION:
                  Action newAction = (Action)builtinStatement;
                  validatorResultBuilder.merge(this.actionContainer.addAction(newAction));
                  break;
               case NOTIFICATION:
                  Notification newNotification = (Notification)builtinStatement;
                  validatorResultBuilder.merge(this.notificationContainer.addNotification(newNotification));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.GROUPING.getQName();
   }

   public synchronized ValidatorResult build(BuildPhase buildPhase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.isBuilding) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         this.isBuilding = true;
         if (buildPhase.compareTo(BuildPhase.GRAMMAR) <= 0) {
            validatorResultBuilder.merge(this.buildChildren(buildPhase));
         }

         this.isBuilding = false;
         return validatorResultBuilder.build();
      }
   }

   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      //this.isValidated = true;
      return validatorResultBuilder.build();
   }

   public List<YangStatement> getReferencedBy() {
      return this.referencedBys;
   }




   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      statements.addAll(this.actionContainer.getActions());
      statements.addAll(this.dataDefContainer.getDataDefChildren());
      statements.addAll(this.groupingDefContainer.getGroupings());
      statements.addAll(this.notificationContainer.getNotifications());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
