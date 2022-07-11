package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.ActionContainer;
import org.yangcentral.yangkit.model.api.stmt.Augment;
import org.yangcentral.yangkit.model.api.stmt.Case;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.DataDefContainer;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.NotificationContainer;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Uses;
import org.yangcentral.yangkit.model.api.stmt.WhenSupport;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

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

   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }

   public SchemaNode getMandatoryDescendant() {
      return this.schemaNodeContainer.getMandatoryDescendant();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator var3 = subElements.iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
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
      Iterator var3;
      SchemaNode child;
      switch (phase) {
         case SCHEMA_BUILD:
            List<DataDefinition> dataDefChildren = this.getDataDefChildren();
            Iterator var9 = dataDefChildren.iterator();

            while(var9.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)var9.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
            }

            var9 = this.getActions().iterator();

            while(var9.hasNext()) {
               Action action = (Action)var9.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(action));
            }

            var9 = this.getNotifications().iterator();

            while(var9.hasNext()) {
               Notification notification = (Notification)var9.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(notification));
            }

            return validatorResultBuilder.build();
         case SCHEMA_EXPAND:
            var3 = this.getSchemaNodeChildren().iterator();

            while(true) {
               while(true) {
                  while(var3.hasNext()) {
                     child = (SchemaNode)var3.next();
                     ValidatorRecordBuilder validatorRecordBuilder;
                     if (child instanceof DataDefinition) {
                        if (!(this.target instanceof DataDefContainer)) {
                           validatorRecordBuilder = new ValidatorRecordBuilder();
                           validatorRecordBuilder.setBadElement(child);
                           validatorRecordBuilder.setSeverity(Severity.ERROR);
                           validatorRecordBuilder.setErrorPath(child.getElementPosition());
                           validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                           validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                           validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        } else if (this.target instanceof Choice && child instanceof Uses) {
                           validatorRecordBuilder = new ValidatorRecordBuilder();
                           validatorRecordBuilder.setBadElement(child);
                           validatorRecordBuilder.setSeverity(Severity.ERROR);
                           validatorRecordBuilder.setErrorPath(child.getElementPosition());
                           validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                           validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                           validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        } else if (this.target instanceof Choice) {
                           Choice choice = (Choice)this.target;
                           Case ch = null;
                           if (child instanceof Case) {
                              ch = (Case)child;
                           } else {
                              ch = new CaseImpl(child.getArgStr());
                              ((Case)ch).setContext(new YangContext(this.getContext()));
                              ((Case)ch).setShortCase(true);
                              ((Case)ch).addDataDefChild((DataDefinition)child);
                              ((Case)ch).addSchemaNodeChild(child);
                              this.removeSchemaNodeChild(child);
                              this.addSchemaNodeChild((SchemaNode)ch);
                           }

                           if (!choice.addCase((Case)ch)) {
                              validatorRecordBuilder = new ValidatorRecordBuilder();
                              validatorRecordBuilder.setBadElement(child);
                              validatorRecordBuilder.setSeverity(Severity.ERROR);
                              validatorRecordBuilder.setErrorPath(child.getElementPosition());
                              validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                              validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                              validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                           }
                        }
                     } else if (child instanceof Action) {
                        if (!(this.target instanceof ActionContainer)) {
                           validatorRecordBuilder = new ValidatorRecordBuilder();
                           validatorRecordBuilder.setBadElement(child);
                           validatorRecordBuilder.setSeverity(Severity.ERROR);
                           validatorRecordBuilder.setErrorPath(child.getElementPosition());
                           validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                           validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                           validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        }
                     } else if (child instanceof Notification && !(this.target instanceof NotificationContainer)) {
                        validatorRecordBuilder = new ValidatorRecordBuilder();
                        validatorRecordBuilder.setBadElement(child);
                        validatorRecordBuilder.setSeverity(Severity.ERROR);
                        validatorRecordBuilder.setErrorPath(child.getElementPosition());
                        validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     }
                  }

                  return validatorResultBuilder.build();
               }
            }
         case SCHEMA_TREE:
            var3 = this.getSchemaNodeChildren().iterator();

            while(var3.hasNext()) {
               child = (SchemaNode)var3.next();
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
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(mandatoryDescendant);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(mandatoryDescendant.getElementPosition());
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.AUGMENT_MANDATORY_NODE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         if (mandatoryDescendant instanceof WhenSupport) {
            WhenSupport whenSupport = (WhenSupport)mandatoryDescendant;
            if (whenSupport.getWhen() != null) {
               validatorRecordBuilder.setSeverity(Severity.WARNING);
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.actionContainer.getActions());
      statements.addAll(this.dataDefContainer.getDataDefChildren());
      statements.addAll(this.notificationContainer.getNotifications());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
