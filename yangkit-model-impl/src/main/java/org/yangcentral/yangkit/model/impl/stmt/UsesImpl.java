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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class UsesImpl extends DataDefinitionImpl implements Uses {
   private Grouping refGrouping;
   private List<Augment> augments = new ArrayList();
   private List<Refine> refines = new ArrayList();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);

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

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.AUGMENT.getQName());
      Iterator var3;
      YangStatement child;
      if (matched.size() > 0) {
         var3 = matched.iterator();

         while(var3.hasNext()) {
            child = (YangStatement)var3.next();
            Augment augment = (Augment)child;
            this.augments.add(augment);
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.REFINE.getQName());
      if (matched.size() > 0) {
         var3 = matched.iterator();

         while(var3.hasNext()) {
            child = (YangStatement)var3.next();
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
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_PREFIX.toString(new String[]{"name=" + fName.getPrefix()})));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return null;
         } else {
            Optional<org.yangcentral.yangkit.model.api.stmt.Module> moduleOptional = this.getContext().getSchemaContext().getModule((ModuleId)moduleIdOp.get());
            if (!moduleOptional.isPresent()) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + ((ModuleId)moduleIdOp.get()).getModuleName()})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return null;
            } else {
               org.yangcentral.yangkit.model.api.stmt.Module targetModule = (Module)moduleOptional.get();
               if (targetModule.getContext().getGrouping(fName.getLocalName()) == null) {
                  ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_GROUPING.toString(new String[]{"name=" + fName.getLocalName()}) + "in module:" + targetModule.getArgStr()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  return null;
               } else {
                  return targetModule.getContext().getGrouping(fName.getLocalName());
               }
            }
         }
      } else if (this.getContext().getGrouping(fName.getLocalName()) == null) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_GROUPING.toString(new String[]{"name=" + fName.getLocalName()})));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return null;
      } else {
         return this.getContext().getGrouping(fName.getLocalName());
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      Iterator iterator;
      Augment augment;
      Refine refine;
      SchemaPath targetPath;
      switch (phase) {
         case GRAMMAR:
            FName fName = new FName(this.getArgStr());
            if (fName.getPrefix() != null) {
               String prefix = fName.getPrefix();
               Import im = this.getContext().getCurModule().getImportByPrefix(prefix);
               if (im != null) {
                  im.addReference(this);
               }
            }

            this.refGrouping = this.buildRefGrouping(validatorResultBuilder);
            this.refGrouping.addReference(this);
            iterator = this.augments.iterator();

            ValidatorRecordBuilder validatorRecordBuilder;
            while(iterator.hasNext()) {
               augment = (Augment)iterator.next();

               try {
                  targetPath = SchemaPathImpl.from(this.getContext().getCurModule(), this, augment,augment.getArgStr());
                  augment.setTargetPath(targetPath);
               } catch (ModelException e) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),e.getSeverity(),
                          ErrorTag.BAD_ELEMENT,e.getDescription()));
               }
            }

            iterator = this.refines.iterator();

            while(iterator.hasNext()) {
               refine = (Refine)iterator.next();

               try {
                  targetPath = SchemaPathImpl.from(this.getContext().getCurModule(), this, refine,refine.getArgStr());
                  refine.setTargetPath(targetPath);
               } catch (ModelException e) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),e.getSeverity(),
                          ErrorTag.BAD_ELEMENT,e.getDescription()));
               }
            }

            return validatorResultBuilder.build();
         case SCHEMA_BUILD:
            List<DataDefinition> dataDefChildren = this.refGrouping.getDataDefChildren();
            iterator = dataDefChildren.iterator();

            int var9;
            while(iterator.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)iterator.next();
               DataDefinition clonedDataDefiniton = (DataDefinition)dataDefinition.clone();
               YangContext newYangContext = new YangContext(dataDefinition.getContext());
               newYangContext.setCurGrouping(this.getContext().getCurGrouping());
               clonedDataDefiniton.setContext(newYangContext);
               clonedDataDefiniton.getContext().setNamespace(this.getContext().getNamespace());
               clonedDataDefiniton.init();
               BuildPhase[] var8 = BuildPhase.values();
               var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  BuildPhase buildPhase = var8[var10];
                  if (buildPhase.compareTo(phase) < 0) {
                     clonedDataDefiniton.build(buildPhase);
                  }
               }

               validatorResultBuilder.merge(clonedDataDefiniton.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedDataDefiniton));
            }

            iterator = this.refGrouping.getActions().iterator();

            BuildPhase[] var26;
            int var28;
            BuildPhase buildPhase;
            while(iterator.hasNext()) {
               Action action = (Action)iterator.next();
               Action clonedAction = (Action)action.clone();
               clonedAction.setContext(new YangContext(action.getContext()));
               clonedAction.getContext().setNamespace(this.getContext().getNamespace());
               clonedAction.init();
               var26 = BuildPhase.values();
               var28 = var26.length;

               for(var9 = 0; var9 < var28; ++var9) {
                  buildPhase = var26[var9];
                  if (buildPhase.compareTo(phase) < 0) {
                     clonedAction.build(buildPhase);
                  }
               }

               validatorResultBuilder.merge(clonedAction.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedAction));
            }

            iterator = this.refGrouping.getNotifications().iterator();

            while(iterator.hasNext()) {
               Notification notification = (Notification)iterator.next();
               Notification clonedNotification = (Notification)notification.clone();
               clonedNotification.setContext(new YangContext(notification.getContext()));
               clonedNotification.getContext().setNamespace(this.getContext().getNamespace());
               clonedNotification.init();
               var26 = BuildPhase.values();
               var28 = var26.length;

               for(var9 = 0; var9 < var28; ++var9) {
                  buildPhase = var26[var9];
                  if (buildPhase.compareTo(phase) < 0) {
                     clonedNotification.build(buildPhase);
                  }
               }

               validatorResultBuilder.merge(clonedNotification.build(phase));
               validatorResultBuilder.merge(this.addSchemaNodeChild(clonedNotification));
            }

            iterator = this.augments.iterator();

            SchemaNode target;
            while(iterator.hasNext()) {
               augment = (Augment)iterator.next();
               targetPath = augment.getTargetPath();
               target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
               if (!(target instanceof Augmentable)) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(augment);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(augment.getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.TARGET_CAN_NOT_AUGMENTED.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  augment.setTarget(target);
                  SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)target;
                  validatorResultBuilder.merge(schemaNodeContainer.addSchemaNodeChild(augment));
               }
            }

            iterator = this.refines.iterator();

            while(iterator.hasNext()) {
               refine = (Refine)iterator.next();
               targetPath = refine.getTargetPath();
               target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
               refine.setTarget(target);
            }

            return validatorResultBuilder.build();
         case SCHEMA_TREE:
            Iterator var3 = this.getSchemaNodeChildren().iterator();

            while(var3.hasNext()) {
               SchemaNode child = (SchemaNode)var3.next();
               validatorResultBuilder.merge(child.build(phase));
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
         return parent instanceof SchemaNode ? ((SchemaNode)parent).isConfig() : true;
      }
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      Iterator var2 = this.getSchemaNodeChildren().iterator();

      while(var2.hasNext()) {
         SchemaNode child = (SchemaNode)var2.next();
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

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.augments);
      statements.addAll(this.refines);
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
