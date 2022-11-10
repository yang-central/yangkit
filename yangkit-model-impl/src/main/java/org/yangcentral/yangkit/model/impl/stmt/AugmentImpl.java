package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AugmentImpl extends DataDefinitionImpl implements Augment {
   private ActionContainerImpl actionContainer = new ActionContainerImpl();
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private NotificationContainerImpl notificationContainer = new NotificationContainerImpl();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   private SchemaPath targetPath;
   private SchemaNode target;

   public AugmentImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.actionContainer.setYangContext(context);
      this.dataDefContainer.setYangContext(context);
      this.notificationContainer.setYangContext(context);
      this.schemaNodeContainer.setYangContext(context);
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

   public SchemaNode getTarget() {
      return this.target;
   }

   public void setTarget(SchemaNode target) {
      this.target = target;
   }

   public SchemaPath getTargetPath() {
      return this.targetPath;
   }

   public void setTargetPath(SchemaPath schemaPath) {
      this.targetPath = schemaPath;
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

   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
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

   @Override
   public boolean checkChild(YangStatement subStatement) {
      boolean result = super.checkChild(subStatement);
      if(!result){
         return false;
      }
      YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
      switch (builtinKeyword){
         case CONTAINER:
         case LIST:
         case LEAF:
         case LEAFLIST:
         case ANYDATA:
         case ANYXML:
         case CHOICE:
         case CASE:
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
      dataDefContainer.removeDataDefs();
      actionContainer.removeActions();
      notificationContainer.removeNotifications();
      this.schemaNodeContainer.removeSchemaNodeChildren();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator elementIterator = subElements.iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
            switch (builtinKeyword) {
               case CONTAINER:
               case LIST:
               case LEAF:
               case LEAFLIST:
               case ANYDATA:
               case ANYXML:
               case CHOICE:
               case CASE:
               case USES:
                  DataDefinition newDataDefinition = (DataDefinition)builtinStatement;
                  validatorResultBuilder.merge(this.addDataDefChild(newDataDefinition));
                  break;
               case ACTION:
                  Action newAction = (Action)builtinStatement;
                  validatorResultBuilder.merge(this.addAction(newAction));
                  break;
               case NOTIFICATION:
                  Notification newNotification = (Notification)builtinStatement;
                  validatorResultBuilder.merge(this.addNotification(newNotification));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      Iterator iterator;
      SchemaNode child;
      switch (phase) {
         case SCHEMA_BUILD:
            List<DataDefinition> dataDefChildren = this.getDataDefChildren();
            Iterator schemaChildrenIt = dataDefChildren.iterator();

            while(schemaChildrenIt.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)schemaChildrenIt.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
            }

            schemaChildrenIt = this.getActions().iterator();

            while(schemaChildrenIt.hasNext()) {
               Action action = (Action)schemaChildrenIt.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(action));
            }

            schemaChildrenIt = this.getNotifications().iterator();

            while(schemaChildrenIt.hasNext()) {
               Notification notification = (Notification)schemaChildrenIt.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(notification));
            }

            return validatorResultBuilder.build();
         case SCHEMA_EXPAND:
            iterator = this.getSchemaNodeChildren().iterator();
            while(iterator.hasNext()) {
               child = (SchemaNode)iterator.next();
               if (child instanceof DataDefinition) {
                  if (!(this.target instanceof DataDefContainer)) {
                     validatorResultBuilder.addRecord(
                             ModelUtil.reportError(child,ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  } else if (this.target instanceof Choice && child instanceof Uses) {
                     validatorResultBuilder.addRecord(
                             ModelUtil.reportError(child,ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  } else if (this.target instanceof Choice) {
                     ChoiceImpl choice = (ChoiceImpl)this.target;
                     Case ch = null;
                     if (child instanceof Case) {
                        ch = (Case)child;
                     } else {
                        ch = new CaseImpl(child.getArgStr());
                        ch.setContext(new YangContext(this.getContext()));
                        ch.setShortCase(true);
                        ch.addDataDefChild((DataDefinition)child);
                        ch.addSchemaNodeChild(child);
                        this.removeSchemaNodeChild(child);
                        this.removeSchemaNodeChild(ch);//remove the old if it's built
                        this.addSchemaNodeChild(ch);
                        ch.init();
                        ch.build();
                     }
                     choice.removeCase(ch.getIdentifier());//remove the old if it's built
                     if (!choice.addCase(ch)) {
                        validatorResultBuilder.addRecord(
                                ModelUtil.reportError(child,ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     }
                  }
               } else if (child instanceof Action) {
                  if (!(this.target instanceof ActionContainer)) {
                     validatorResultBuilder.addRecord(
                             ModelUtil.reportError(child,ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  }
               } else if (child instanceof Notification && !(this.target instanceof NotificationContainer)) {
                  validatorResultBuilder.addRecord(
                          ModelUtil.reportError(child,ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            return validatorResultBuilder.build();
         case SCHEMA_TREE:
            iterator = this.getSchemaNodeChildren().iterator();

            while(iterator.hasNext()) {
               child = (SchemaNode)iterator.next();
               if (child instanceof Case && ((Case)child).isShortCase()) {
                  validatorResultBuilder.merge(child.build(phase));
               }
            }
      }

      return validatorResultBuilder.build();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.AUGMENT.getQName();
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

   public boolean isConfig() {
      if (this.getSchemaTreeType() != SchemaTreeType.DATATREE) {
         return false;
      } else {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         return parent instanceof SchemaNode ? ((SchemaNode)parent).isConfig() : true;
      }
   }

   public QName getIdentifier() {
      return new QName(this.getContext().getNamespace(), this.getArgStr() + this.hashCode());
   }

   public SchemaPath.Absolute getSchemaPath() {
      throw new IllegalArgumentException("un-support");
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof AugmentImpl)) {
         return false;
      } else {
         AugmentImpl augment = (AugmentImpl)o;
         return this.getTargetPath().equals(augment.getTargetPath());
      }
   }

   public int hashCode() {
      return Objects.hash(schemaNodeContainer,
              getTargetPath());
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      if (this.isMandatory()) {
         SchemaNode mandatoryDescendant = this.getMandatoryDescendant();
         Severity severity = Severity.ERROR;
         if (mandatoryDescendant instanceof WhenSupport) {
            WhenSupport whenSupport = (WhenSupport)mandatoryDescendant;
            if (whenSupport.getWhen() != null) {
               severity = Severity.WARNING;
            }
         }
         validatorResultBuilder.addRecord(
                 ModelUtil.reportError(mandatoryDescendant,severity, ErrorTag.BAD_ELEMENT,
                         ErrorCode.AUGMENT_MANDATORY_NODE.getFieldName()));

      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(getEffectiveSchemaNodeChildren());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
