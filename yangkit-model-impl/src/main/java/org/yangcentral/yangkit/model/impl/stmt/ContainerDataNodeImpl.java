package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.mount.MountPoint;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.ContainerDataNode;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class ContainerDataNodeImpl extends DataNodeImpl implements ContainerDataNode {
   private ActionContainerImpl actionContainer = new ActionContainerImpl();
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
   private List<MountPoint> mountPoints = new ArrayList();
   private NotificationContainerImpl notificationContainer = new NotificationContainerImpl();
   private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);

   public ContainerDataNodeImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.typedefContainer.setYangContext(context);
      this.groupingDefContainer.setYangContext(context);
      this.schemaNodeContainer.setYangContext(context);
      this.dataDefContainer.setYangContext(context);
      this.actionContainer.setYangContext(context);
      this.notificationContainer.setYangContext(context);
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

   public List<MountPoint> getMountPoints() {
      return Collections.unmodifiableList(this.mountPoints);
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
      boolean result =  super.checkChild(subStatement);
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
      //clear current state
      this.typedefContainer.removeTypedefs();
      this.groupingDefContainer.removeGroupings();
      this.dataDefContainer.removeDataDefs();
      this.actionContainer.removeActions();
      this.notificationContainer.removeNotifications();
      this.schemaNodeContainer.removeSchemaNodeChildren();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangElement> subElements = this.getSubElements();
      Iterator iterator = subElements.iterator();

      while(iterator.hasNext()) {
         YangElement subElement = (YangElement)iterator.next();
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

   public List<SchemaNode> getSchemaNodeChildren() {
      return this.schemaNodeContainer.getSchemaNodeChildren();
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      return this.schemaNodeContainer.addSchemaNodeChild(schemaNode);
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      return this.schemaNodeContainer.addSchemaNodeChildren(schemaNodes);
   }

   public SchemaNode getSchemaNodeChild(QName identifier) {
      return this.schemaNodeContainer.getSchemaNodeChild(identifier);
   }

   public DataNode getDataNodeChild(QName identifier) {
      return this.schemaNodeContainer.getDataNodeChild(identifier);
   }

   public List<DataNode> getDataNodeChildren() {
      return this.schemaNodeContainer.getDataNodeChildren();
   }

   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }

   public SchemaNode getMandatoryDescendant() {
      return this.schemaNodeContainer.getMandatoryDescendant();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_BUILD:
            Iterator iterator = this.getDataDefChildren().iterator();

            while(iterator.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)iterator.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
            }

            iterator = this.getActions().iterator();

            while(iterator.hasNext()) {
               Action action = (Action)iterator.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(action));
            }

            iterator = this.getNotifications().iterator();

            while(iterator.hasNext()) {
               Notification notification = (Notification)iterator.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(notification));
            }
         default:
            return validatorResultBuilder.build();
      }
   }
   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
   }
   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(getEffectiveSchemaNodeChildren());
      statements.addAll(this.groupingDefContainer.getGroupings());
      statements.addAll(this.typedefContainer.getTypedefs());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
