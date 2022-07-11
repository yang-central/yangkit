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
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupingImpl extends EntityImpl implements Grouping {
   private ActionContainerImpl actionContainer = new ActionContainerImpl();
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
   private NotificationContainerImpl notificationContainer = new NotificationContainerImpl();
   private List<YangStatement> referencedBys = new ArrayList();
   private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();

   public void setContext(YangContext context) {
      super.setContext(context);
      this.actionContainer.setYangContext(context);
      this.dataDefContainer.setYangContext(context);
      this.groupingDefContainer.setYangContext(context);
      this.notificationContainer.setYangContext(context);
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

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator var3 = subElements.iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
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
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
      this.isValidated = true;
      return validatorResultBuilder.build();
   }

   public List<YangStatement> getReferencedBy() {
      return this.referencedBys;
   }

   public void addReference(YangStatement yangStatement) {
      this.referencedBys.add(yangStatement);
   }

   public void delReference(YangStatement yangStatement) {
      int pos = -1;

      for(int i = 0; i < this.referencedBys.size(); ++i) {
         if (this.referencedBys.get(i) == yangStatement) {
            pos = i;
            break;
         }
      }

      if (pos != -1) {
         this.referencedBys.remove(pos);
      }

   }

   public boolean isReferencedBy(YangStatement yangStatement) {
      Iterator var2 = this.referencedBys.iterator();

      YangStatement statement;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         statement = (YangStatement)var2.next();
      } while(statement != yangStatement);

      return true;
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.actionContainer.getActions());
      statements.addAll(this.dataDefContainer.getDataDefChildren());
      statements.addAll(this.groupingDefContainer.getGroupings());
      statements.addAll(this.notificationContainer.getNotifications());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
