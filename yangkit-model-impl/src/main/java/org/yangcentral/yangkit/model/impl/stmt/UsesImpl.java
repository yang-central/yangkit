package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.Augment;
import org.yangcentral.yangkit.model.api.stmt.Augmentable;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Refine;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Uses;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.impl.schema.SchemaPathImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsesImpl extends DataDefinitionImpl implements Uses {
   private Grouping refGrouping;
   private final List<Augment> augments = new ArrayList<>();
   private final List<Refine> refines = new ArrayList<>();
   private final SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);

   public UsesImpl(String argStr) {
      super(argStr);
   }

   public Grouping getRefGrouping() {
      return this.refGrouping;
   }

   public List<Augment> getAugments() {
      return Collections.unmodifiableList(this.augments);
   }

   public List<Refine> getRefines() {
      return Collections.unmodifiableList(this.refines);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.USES.getQName();
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.schemaNodeContainer.setYangContext(context);
   }

   @Override
   protected void clearSelf() {
      this.augments.clear();
      this.refines.clear();
      this.schemaNodeContainer.removeSchemaNodeChildren();
      this.refGrouping = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.AUGMENT.getQName());
      if (matched.size() > 0) {
         for (YangStatement child : matched) {
            Augment augment = (Augment)child;
            this.augments.add(augment);
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFINE.getQName());
      if (matched.size() > 0) {
         for (YangStatement child : matched) {
            Refine refine = (Refine)child;
            this.refines.add(refine);
         }
      }

      return validatorResultBuilder.build();
   }

   private Grouping buildRefGrouping(ValidatorResultBuilder validatorResultBuilder) {
      FName fName = new FName(this.getArgStr());
      if (fName.getPrefix() != null && !this.getContext().getCurModule().isSelfPrefix(fName.getPrefix())) {
         Optional<ModuleId> moduleIdOp = this.getContext().getCurModule().findModuleByPrefix(fName.getPrefix());
         if (!moduleIdOp.isPresent()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INVALID_PREFIX.toString(new String[]{"name=" + fName.getPrefix()})));
            return null;
         }

         Optional<Module> moduleOptional = this.getContext().getSchemaContext().getModule(moduleIdOp.get());
         if (!moduleOptional.isPresent()) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + moduleIdOp.get().getModuleName()})));
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + moduleIdOp.get().getModuleName()}) ));
            return null;
         }
         Module targetModule = moduleOptional.get();
         if (targetModule.getContext().getGrouping(fName.getLocalName()) == null) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.MISSING_GROUPING.toString(new String[]{"name=" + fName.getLocalName()})
                            + "in module:" + targetModule.getArgStr()));
            return null;
         } return targetModule.getContext().getGrouping(fName.getLocalName());

      } else if (this.getContext().getGrouping(fName.getLocalName()) == null) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_GROUPING.toString(new String[]{"name=" + fName.getLocalName()})));
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MISSING_GROUPING.toString(new String[]{"name=" + fName.getLocalName()}) ));
         return null;
      }
      return this.getContext().getGrouping(fName.getLocalName());
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case GRAMMAR:{
            FName fName = new FName(this.getArgStr());
            if (fName.getPrefix() != null) {
               String prefix = fName.getPrefix();
               Import im = this.getContext().getCurModule().getImportByPrefix(prefix);
               if (im != null) {
                  if(!im.isReferencedBy(this)){
                     im.addReference(this);
                  }

               }
            }
            //build ref grouping
            this.refGrouping = this.buildRefGrouping(validatorResultBuilder);
            if(this.refGrouping != null){
               if(!this.refGrouping.isReferencedBy(this)){
                  this.refGrouping.addReference(this);
               }
            }
            else {
               return validatorResultBuilder.build();
            }

            break;
         }

         case SCHEMA_BUILD:{

            //build datadef
            List<DataDefinition> dataDefChildren = this.refGrouping.getDataDefChildren();
            for (DataDefinition dataDefinition : dataDefChildren) {
               DataDefinition clonedDataDefiniton = (DataDefinition) dataDefinition.clone();
               YangContext newYangContext = dataDefinition.getContext().clone();
               newYangContext.setCurGrouping(this.getContext().getCurGrouping());
               clonedDataDefiniton.setContext(newYangContext);
               clonedDataDefiniton.getContext().setNamespace(this.getContext().getNamespace());
               clonedDataDefiniton.init();
               for (BuildPhase buildPhase : BuildPhase.values()) {
                  if (buildPhase.compareTo(phase) <= 0) {
                     ValidatorResult phaseResult = clonedDataDefiniton.build(buildPhase);
                     validatorResultBuilder.merge(phaseResult);
                     if (!phaseResult.isOk()) {
                        break;
                     }
                  }
               }

               //validatorResultBuilder.merge(clonedDataDefiniton.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedDataDefiniton));
            }
            //build action
            for (Action action : this.refGrouping.getActions()) {
               Action clonedAction = (Action) action.clone();
               clonedAction.setContext(action.getContext().clone());
               clonedAction.getContext().setNamespace(this.getContext().getNamespace());
               clonedAction.getContext().setCurGrouping(this.getContext().getCurGrouping());
               clonedAction.init();


               for (BuildPhase buildPhase : BuildPhase.values()) {
                  if (buildPhase.compareTo(phase) < 0) {
                     clonedAction.build(buildPhase);
                  }
               }

               validatorResultBuilder.merge(clonedAction.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedAction));
            }

            for (Notification notification : this.refGrouping.getNotifications()) {
               Notification clonedNotification = (Notification) notification.clone();
               clonedNotification.setContext(notification.getContext().clone());
               clonedNotification.getContext().setNamespace(this.getContext().getNamespace());
               clonedNotification.getContext().setCurGrouping(this.getContext().getCurGrouping());
               clonedNotification.init();
               for (BuildPhase buildPhase : BuildPhase.values()) {
                  if (buildPhase.compareTo(phase) < 0) {
                     clonedNotification.build(buildPhase);
                  }
               }

               validatorResultBuilder.merge(clonedNotification.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedNotification));
            }
            //build augments
            for (Augment augment : this.augments) {
               augment.setTargetPath(null);
               try {
                  SchemaPath targetPath = SchemaPathImpl.from(
                      this, augment, augment.getArgStr());
                  augment.setTargetPath(targetPath);
                  SchemaNode target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
                  if (target == null) {
                     continue;
                  }
                  if (!(target instanceof Augmentable)) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(augment,
                         ErrorCode.TARGET_CAN_NOT_AUGMENTED.getFieldName()));
                     continue;
                  }
                  augment.setTarget(target);
                  SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) target;
                  validatorResultBuilder.merge(schemaNodeContainer.addSchemaNodeChild(augment));
               } catch (ModelException e) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(augment, e.getSeverity(),
                      ErrorTag.BAD_ELEMENT, e.getDescription()));
                  continue;
               }
            }

            for (Refine refine : this.refines) {
               //refine.setTargetPath(null);
               try {
                  SchemaPath targetPath = SchemaPathImpl.from(this, refine, refine.getArgStr());
                  refine.setTargetPath(targetPath);
                  SchemaNode target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
                  if (null == target) {
                     continue;
                  }
                  refine.setTarget(target);
               } catch (ModelException e) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(refine, e.getSeverity(),
                      ErrorTag.BAD_ELEMENT, e.getDescription()));
                  continue;
               }
            }
            break;
         }

         case SCHEMA_TREE:{
            for (SchemaNode child : this.getSchemaNodeChildren()) {
               validatorResultBuilder.merge(child.build(phase));
            }
            break;
         }

      }

      return validatorResultBuilder.build();
   }

   public SchemaPath.Absolute getSchemaPath() {
      throw new IllegalArgumentException("no schema path");
   }

   public boolean isConfig() {
      if (this.getSchemaTreeType() != SchemaTreeType.DATATREE) {
         return false;
      } else {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         return !(parent instanceof SchemaNode) || ((SchemaNode) parent).isConfig();
      }
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());

      for (SchemaNode child : this.getSchemaNodeChildren()) {
         validatorResultBuilder.merge(child.validate());
      }

      return validatorResultBuilder.build();
   }

   public QName getIdentifier() {
      return new QName(this.getContext().getNamespace(), this.getYangKeyword().getLocalName() + ":" + this.getArgStr());
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

   public YangStatement getReferenceStatement() {
      return this.refGrouping;
   }
   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      return this.schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
   }
   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      statements.addAll(getEffectiveSchemaNodeChildren());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
