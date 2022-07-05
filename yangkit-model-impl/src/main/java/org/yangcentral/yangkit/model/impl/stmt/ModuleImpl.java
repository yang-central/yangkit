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
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.Augment;
import org.yangcentral.yangkit.model.api.stmt.Augmentable;
import org.yangcentral.yangkit.model.api.stmt.Contact;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Deviation;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Feature;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.Include;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Organization;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.Revision;
import org.yangcentral.yangkit.model.api.stmt.Rpc;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.Uses;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangVersion;
import org.yangcentral.yangkit.model.impl.schema.SchemaPathImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class ModuleImpl extends YangStatementImpl implements Module {
   private YangVersion yangVersion;
   private Description description;
   private Reference reference;
   private Organization organization;
   private Contact contact;
   private List<Import> imports = new ArrayList();
   private List<Include> includes = new ArrayList();
   private List<Revision> revisions = new ArrayList();
   private List<Extension> extensions = new ArrayList();
   private List<Feature> features = new ArrayList();
   private List<Identity> identities = new ArrayList();
   private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
   private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private List<Rpc> rpcs = new ArrayList();
   private NotificationContainerImpl notificationContainer = new NotificationContainerImpl();
   private List<Augment> augments = new ArrayList();
   private List<Deviation> deviations = new ArrayList();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   protected Map<String, ModuleId> prefixCache = new ConcurrentHashMap();

   public ModuleImpl(String argStr) {
      super(argStr);
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

   public SchemaNode getMandatoryDescendant() {
      return this.schemaNodeContainer.getMandatoryDescendant();
   }

   public List<Grouping> getGroupings() {
      return this.groupingDefContainer.getGroupings();
   }

   public Grouping getGrouping(String name) {
      return this.groupingDefContainer.getGrouping(name);
   }

   public Description getDescription() {
      return this.description;
   }

   public void setDescription(Description description) {
      this.description = description;
   }

   public Reference getReference() {
      return this.reference;
   }

   public void setReference(Reference reference) {
      this.reference = reference;
   }

   public YangVersion getYangVersion() {
      return this.yangVersion;
   }

   public String getEffectiveYangVersion() {
      return null == this.getYangVersion() ? "1" : this.getYangVersion().getArgStr();
   }

   public List<Include> getIncludes() {
      return Collections.unmodifiableList(this.includes);
   }

   public List<Import> getImports() {
      return Collections.unmodifiableList(this.imports);
   }

   public Organization getOrganization() {
      return this.organization;
   }

   public Contact getContact() {
      return this.contact;
   }

   public List<Revision> getRevisions() {
      return Collections.unmodifiableList(this.revisions);
   }

   public Optional<Revision> getCurRevision() {
      return this.revisions.size() == 0 ? Optional.empty() : Optional.of((Revision)this.revisions.get(0));
   }

   public Optional<String> getCurRevisionDate() {
      Optional<Revision> revisionOptional = this.getCurRevision();
      return !revisionOptional.isPresent() ? Optional.empty() : Optional.of(((Revision)revisionOptional.get()).getArgStr());
   }

   public List<Extension> getExtensions() {
      return Collections.unmodifiableList(this.extensions);
   }

   public Extension getExtension(String name) {
      return (Extension)this.getContext().getExtensionCache().get(name);
   }

   public List<Feature> getFeatures() {
      return Collections.unmodifiableList(this.features);
   }

   public Feature getFeature(String name) {
      return (Feature)this.getContext().getFeatureCache().get(name);
   }

   public List<Identity> getIdentities() {
      return Collections.unmodifiableList(this.identities);
   }

   public Identity getIdentity(String name) {
      return (Identity)this.getContext().getIdentityCache().get(name);
   }

   public List<Augment> getAugments() {
      return Collections.unmodifiableList(this.augments);
   }

   public List<Rpc> getRpcs() {
      return Collections.unmodifiableList(this.rpcs);
   }

   public Rpc getRpc(String name) {
      SchemaNode schemaNode = (SchemaNode)this.getContext().getSchemaNodeIdentifierCache().get(name);
      return null != schemaNode && schemaNode instanceof Rpc ? (Rpc)schemaNode : null;
   }

   public List<Deviation> getDeviations() {
      return Collections.unmodifiableList(this.deviations);
   }

   public Map<String, ModuleId> getPrefixes() {
      return this.prefixCache;
   }

   public ModuleId getModuleId() {
      List<YangStatement> revisions = this.getSubStatement(YangBuiltinKeyword.REVISION.getQName());
      return revisions.isEmpty() ? new ModuleId(this.getArgStr(), "") : new ModuleId(this.getArgStr(), ((YangStatement)revisions.get(0)).getArgStr());
   }

   public Optional<ModuleId> findModuleByPrefix(String prefix) {
      ModuleId moduleId = (ModuleId)this.prefixCache.get(prefix);
      return null == moduleId ? Optional.empty() : Optional.of(moduleId);
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

   private <T extends YangStatement> ValidatorResult mergeDefintion(Map<String, T> source, List<T> candidate) {
      return this.mergeDefintion(source, candidate, false);
   }

   private <T extends YangStatement> ValidatorResult mergeDefintion(Map<String, T> source, List<T> candidate, boolean ignoreDuplicate) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator<T> iterator = candidate.iterator();

      while(iterator.hasNext()) {
         T entry = iterator.next();
         if (source.containsKey(entry.getArgStr())) {
            if (!ignoreDuplicate) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(entry);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(entry.getElementPosition());
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName() + " in " + ((YangStatement)source.get(entry.getArgStr())).getElementPosition()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         } else {
            source.put(entry.getArgStr(), entry);
         }
      }

      return validatorResultBuilder.build();
   }

   private <T extends YangStatement> ValidatorResult mergeDefintion(Map<String, T> source, Map<String, T> candidate) {
      return this.mergeDefintion(source, candidate, false);
   }

   private <T extends YangStatement> ValidatorResult mergeDefintion(Map<String, T> source, Map<String, T> candidate, boolean ignoreDuplicate) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator<Map.Entry<String, T>> iterator = candidate.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<String, T> entry = iterator.next();
         if (source.containsKey(entry.getKey())) {
            if (!ignoreDuplicate) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(entry.getValue());
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath((entry.getValue()).getElementPosition());
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName() + " in " + ((YangStatement)source.get(entry.getKey())).getElementPosition()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         } else {
            source.put(entry.getKey(), entry.getValue());
         }
      }

      return validatorResultBuilder.build();
   }

   private <T extends YangStatement> ValidatorResult mergeDefintion(List<T> source, List<T> candidate) {
      return this.mergeDefintion(source, candidate, false);
   }

   private <T extends YangStatement> ValidatorResult mergeDefintion(List<T> source, List<T> candidate, boolean ignoreDuplicate) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator<T> iterator = candidate.iterator();
      Map<String, T> sourceMap = source.stream().collect(Collectors.toMap(YangStatement::getArgStr, YangStatement::getSelf));

      while(iterator.hasNext()) {
         T entry = iterator.next();
         if (sourceMap.containsKey(entry.getArgStr())) {
            if (!ignoreDuplicate) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setBadElement(entry);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(entry.getElementPosition());
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName() + " in " + ((YangStatement)sourceMap.get(entry.getArgStr())).getElementPosition()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         } else {
            source.add(entry);
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult mergeSubModules() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var2 = this.includes.iterator();

      while(true) {
         Include include;
         do {
            if (!var2.hasNext()) {
               return validatorResultBuilder.build();
            }

            include = (Include)var2.next();
         } while(!include.getInclude().isPresent());

         SubModule includeModule = (SubModule)include.getInclude().get();
         validatorResultBuilder.merge(this.mergeDefintion(this.getContext().getTypedefIdentifierCache(), includeModule.getTypedefs()));
         validatorResultBuilder.merge(this.mergeDefintion(this.getContext().getGroupingIdentifierCache(), includeModule.getGroupings()));
         Iterator var5 = includeModule.getDataDefChildren().iterator();

         while(var5.hasNext()) {
            DataDefinition dataDefinition = (DataDefinition)var5.next();
            if (!(dataDefinition instanceof Uses)) {
               this.getContext().getSchemaNodeIdentifierCache().put(dataDefinition.getArgStr(), dataDefinition);
            }
         }

         var5 = includeModule.getRpcs().iterator();

         while(var5.hasNext()) {
            Rpc rpc = (Rpc)var5.next();
            this.getContext().getSchemaNodeIdentifierCache().put(rpc.getArgStr(), rpc);
         }

         var5 = includeModule.getNotifications().iterator();

         while(var5.hasNext()) {
            Notification notification = (Notification)var5.next();
            this.getContext().getSchemaNodeIdentifierCache().put(notification.getArgStr(), notification);
         }

         validatorResultBuilder.merge(this.mergeDefintion(this.getContext().getExtensionCache(), includeModule.getExtensions()));
         validatorResultBuilder.merge(this.mergeDefintion(this.getContext().getIdentityCache(), includeModule.getIdentities()));
         validatorResultBuilder.merge(this.mergeDefintion(this.getContext().getFeatureCache(), includeModule.getFeatures()));
      }
   }

   private ValidatorResult mergeSubModulesSchemaTree() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var2 = this.includes.iterator();

      while(true) {
         Include include;
         do {
            if (!var2.hasNext()) {
               return validatorResultBuilder.build();
            }

            include = (Include)var2.next();
         } while(!include.getInclude().isPresent());

         SubModule includeModule = (SubModule)include.getInclude().get();
         Iterator var5 = includeModule.getSchemaNodeChildren().iterator();

         while(var5.hasNext()) {
            SchemaNode schemaNode = (SchemaNode)var5.next();
            validatorResultBuilder.merge(this.addSchemaNodeChild(schemaNode));
         }
      }
   }

   private void updateSubModules() {
      Iterator var1 = this.includes.iterator();

      while(var1.hasNext()) {
         Include include = (Include)var1.next();
         if (include.getInclude().isPresent()) {
            SubModule includeModule = (SubModule)include.getInclude().get();
            this.mergeDefintion(includeModule.getContext().getTypedefIdentifierCache(), this.getContext().getTypedefIdentifierCache(), true);
            this.mergeDefintion(includeModule.getContext().getGroupingIdentifierCache(), this.getContext().getGroupingIdentifierCache(), true);
            this.mergeDefintion(includeModule.getContext().getSchemaNodeIdentifierCache(), this.getContext().getSchemaNodeIdentifierCache(), true);
            this.mergeDefintion(includeModule.getContext().getExtensionCache(), this.getContext().getExtensionCache(), true);
            this.mergeDefintion(includeModule.getContext().getIdentityCache(), this.getContext().getIdentityCache(), true);
            this.mergeDefintion(includeModule.getContext().getFeatureCache(), this.getContext().getFeatureCache(), true);
         }
      }

   }

   private ValidatorResult validateSubModules(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.includes.iterator();

      while(var3.hasNext()) {
         Include include = (Include)var3.next();
         if (include.getInclude().isPresent()) {
            SubModule includeModule = (SubModule)include.getInclude().get();
            if (!includeModule.isBuilt()) {
               validatorResultBuilder.merge(includeModule.build(phase));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateSubModules(BuildPhase from, BuildPhase to) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var4 = this.includes.iterator();

      while(true) {
         SubModule includeModule;
         do {
            Include include;
            do {
               if (!var4.hasNext()) {
                  return validatorResultBuilder.build();
               }

               include = (Include)var4.next();
            } while(!include.getInclude().isPresent());

            includeModule = (SubModule)include.getInclude().get();
         } while(includeModule.isBuilt());

         BuildPhase[] var7 = BuildPhase.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            BuildPhase buildPhase = var7[var9];
            if (buildPhase.compareTo(from) < 0 || buildPhase.compareTo(to) > 0) {
               break;
            }

            ValidatorResult phaseResult = includeModule.build(buildPhase);
            validatorResultBuilder.merge(phaseResult);
            if (!phaseResult.isOk()) {
               break;
            }
         }
      }
   }

   private ValidatorResult validateSubModules() {
      return this.validateSubModules(BuildPhase.LINKAGE, BuildPhase.SCHEMA_TREE);
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      Iterator var13;
      switch (phase) {
         case GRAMMAR:
            if (this.getEffectiveYangVersion().equals("1.1")) {
               if (this instanceof MainModule) {
                  validatorResultBuilder.merge(this.mergeSubModules());
                  this.updateSubModules();
                  validatorResultBuilder.merge(this.validateSubModules(BuildPhase.LINKAGE, phase));
               }
            } else {
               validatorResultBuilder.merge(this.mergeSubModules());
               validatorResultBuilder.merge(this.validateSubModules());
            }

            var13 = this.augments.iterator();

            while(var13.hasNext()) {
               Augment augment = (Augment)var13.next();

               ValidatorRecordBuilder validatorRecordBuilder;
               try {
                  SchemaPath targetPath = SchemaPathImpl.from(this, this, augment.getArgStr());
                  if (targetPath instanceof SchemaPath.Descendant) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setBadElement(augment);
                     validatorRecordBuilder.setErrorPath(augment.getElementPosition());
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     augment.setTargetPath(targetPath);
                  }
               } catch (ModelException var12) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(var12.getSeverity());
                  validatorRecordBuilder.setBadElement(var12.getElement());
                  validatorRecordBuilder.setErrorPath(var12.getElement().getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(var12.getDescription()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            return validatorResultBuilder.build();
         case SCHEMA_BUILD:
            if (this.getEffectiveYangVersion().equals("1.1") && this instanceof MainModule) {
               validatorResultBuilder.merge(this.validateSubModules(phase));
            }

            var13 = this.getDataDefChildren().iterator();

            while(var13.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)var13.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
            }

            var13 = this.rpcs.iterator();

            while(var13.hasNext()) {
               Rpc rpc = (Rpc)var13.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(rpc));
            }

            var13 = this.getNotifications().iterator();

            while(var13.hasNext()) {
               Notification notification = (Notification)var13.next();
               validatorResultBuilder.merge(this.addSchemaNodeChild(notification));
            }

            if (this.getEffectiveYangVersion().equals("1")) {
               this.getContext().getSchemaContext().addSchemaNodeChildren(this.getSchemaNodeChildren());
               if (this instanceof MainModule) {
                  validatorResultBuilder.merge(this.mergeSubModulesSchemaTree());
               }
            } else if (this instanceof MainModule) {
               validatorResultBuilder.merge(this.mergeSubModulesSchemaTree());
               this.getContext().getSchemaContext().addSchemaNodeChildren(this.getSchemaNodeChildren());
            }
            break;
         case SCHEMA_EXPAND:
            if (this.getEffectiveYangVersion().equals("1.1") && this instanceof MainModule) {
               validatorResultBuilder.merge(this.validateSubModules(phase));
            }

            List<Augment> augmentCache = new ArrayList();
            augmentCache.addAll(this.augments);
            List<Augment> errorCache = new ArrayList();
            ValidatorResultBuilder subResultBuilder = new ValidatorResultBuilder();

            int parsedCount;
            do {
               subResultBuilder.clear();
               parsedCount = 0;
               Iterator var7 = augmentCache.iterator();

               while(var7.hasNext()) {
                  Augment augment = (Augment)var7.next();
                  SchemaPath targetPath = augment.getTargetPath();
                  SchemaNode target = targetPath.getSchemaNode(this.getContext().getSchemaContext());
                  ValidatorRecordBuilder validatorRecordBuilder;
                  if (target == null) {
                     targetPath.getSchemaNode(this.getContext().getSchemaContext());
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(augment.getElementPosition());
                     validatorRecordBuilder.setBadElement(augment);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_TARGET.getFieldName()));
                     subResultBuilder.addRecord(validatorRecordBuilder.build());
                     errorCache.add(augment);
                  } else if (!(target instanceof Augmentable)) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setBadElement(augment);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(augment.getElementPosition());
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.TARGET_CAN_NOT_AUGMENTED.getFieldName()));
                     subResultBuilder.addRecord(validatorRecordBuilder.build());
                     errorCache.add(augment);
                  } else {
                     ++parsedCount;
                     augment.setTarget(target);
                     SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)target;
                     subResultBuilder.merge(schemaNodeContainer.addSchemaNodeChild(augment));
                  }
               }

               augmentCache.clear();
               augmentCache.addAll(errorCache);
               errorCache.clear();
            } while(parsedCount > 0);

            validatorResultBuilder.merge(subResultBuilder.build());
            break;
         case SCHEMA_MODIFIER:
         case SCHEMA_TREE:
            if (this.getEffectiveYangVersion().equals("1.1") && this instanceof MainModule) {
               validatorResultBuilder.merge(this.validateSubModules(phase));
            }
      }

      return validatorResultBuilder.build();
   }

   public void setChildren(List<YangElement> yangElements) {
      this.clear();
      super.setChildren(yangElements);
   }

   protected void clear() {
      this.yangVersion = null;
      this.description = null;
      this.reference = null;
      this.organization = null;
      this.contact = null;
      this.imports.clear();
      this.includes.clear();
      this.revisions.clear();
      this.extensions.clear();
      this.features.clear();
      this.identities.clear();
      this.rpcs.clear();
      this.augments.clear();
      this.deviations.clear();
      super.clear();
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.groupingDefContainer.setYangContext(context);
      this.typedefContainer.setYangContext(context);
      this.schemaNodeContainer.setYangContext(context);
      this.dataDefContainer.setYangContext(context);
      this.notificationContainer.setYangContext(context);
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorResult superValidatorResult = super.initSelf();
      validatorResultBuilder.merge(superValidatorResult);
      List<YangElement> subElements = this.getSubElements();
      Iterator var4 = subElements.iterator();

      while(true) {
         while(true) {
            YangElement subElement;
            do {
               if (!var4.hasNext()) {
                  return validatorResultBuilder.build();
               }

               subElement = (YangElement)var4.next();
            } while(!(subElement instanceof YangBuiltinStatement));

            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
            switch (builtinKeyword) {
               case YANGVERSION:
                  this.yangVersion = (YangVersion)builtinStatement;
                  break;
               case DESCRIPTION:
                  this.description = (Description)builtinStatement;
                  break;
               case REFERENCE:
                  this.reference = (Reference)builtinStatement;
                  break;
               case ORGANIZATION:
                  this.organization = (Organization)builtinStatement;
                  break;
               case CONTACT:
                  this.contact = (Contact)builtinStatement;
                  break;
               case IMPORT:
                  Import newImport = (Import)builtinStatement;
                  Import imp = (Import) ModelUtil.checkConflict(newImport, this.imports);
                  if (null != imp) {
                     if (imp.getRevisionDate() != null && newImport.getRevisionDate() != null) {
                        if (imp.getRevisionDate().getArgStr().equals(newImport.getRevisionDate().getArgStr())) {
                           validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(imp, newImport));
                           newImport.setErrorStatement(true);
                        } else {
                           this.imports.add((Import)builtinStatement);
                        }
                        break;
                     }

                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(imp, newImport));
                     newImport.setErrorStatement(true);
                     break;
                  }

                  this.imports.add(newImport);
                  break;
               case INCLUDE:
                  Include newInclude = (Include)builtinStatement;
                  Include include = (Include)ModelUtil.checkConflict(newInclude, this.includes);
                  if (null != include) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(include, newInclude));
                     newInclude.setErrorStatement(true);
                  } else {
                     this.includes.add(newInclude);
                  }
                  break;
               case REVISION:
                  Revision newRevision = (Revision)builtinStatement;
                  Revision revision = (Revision)ModelUtil.checkConflict(newRevision, this.revisions);
                  if (null != revision) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(revision, newRevision, Severity.WARNING));
                  } else {
                     this.revisions.add(newRevision);
                  }
                  break;
               case EXTENSION:
                  Extension newExtension = (Extension)builtinStatement;
                  if (this.getContext().getExtensionCache().containsKey(builtinStatement.getArgStr())) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError((YangStatement)this.getContext().getExtensionCache().get(builtinStatement.getArgStr()), newExtension));
                     newExtension.setErrorStatement(true);
                  } else {
                     this.extensions.add(newExtension);
                     this.getContext().getExtensionCache().put(builtinStatement.getArgStr(), newExtension);
                  }
                  break;
               case FEATURE:
                  Feature newFeature = (Feature)builtinStatement;
                  if (this.getContext().getFeatureCache().containsKey(builtinStatement.getArgStr())) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError((YangStatement)this.getContext().getFeatureCache().get(builtinStatement.getArgStr()), newFeature));
                     newFeature.setErrorStatement(true);
                  } else {
                     this.features.add(newFeature);
                     this.getContext().getFeatureCache().put(builtinStatement.getArgStr(), newFeature);
                  }
                  break;
               case IDENTITY:
                  Identity newIdentity = (Identity)builtinStatement;
                  if (this.getContext().getIdentityCache().containsKey(newIdentity.getArgStr())) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError((YangStatement)this.getContext().getIdentityCache().get(newIdentity.getArgStr()), newIdentity));
                     newIdentity.setErrorStatement(true);
                  } else {
                     this.identities.add(newIdentity);
                     this.getContext().getIdentityCache().put(newIdentity.getArgStr(), newIdentity);
                  }
                  break;
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
               case RPC:
                  Rpc newRpc = (Rpc)builtinStatement;
                  if (this.getContext().getSchemaNodeIdentifierCache().containsKey(newRpc.getArgStr())) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError((YangStatement)this.getContext().getSchemaNodeIdentifierCache().get(newRpc.getArgStr()), newRpc));
                     newRpc.setErrorStatement(true);
                  } else {
                     this.rpcs.add(newRpc);
                     this.getContext().getSchemaNodeIdentifierCache().put(newRpc.getArgStr(), newRpc);
                  }
                  break;
               case NOTIFICATION:
                  Notification newNotification = (Notification)builtinStatement;
                  validatorResultBuilder.merge(this.notificationContainer.addNotification(newNotification));
                  break;
               case AUGMENT:
                  Augment augment = (Augment)builtinStatement;
                  this.augments.add(augment);
                  break;
               case DEVIATION:
                  this.deviations.add((Deviation)builtinStatement);
            }
         }
      }
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

   public Import getImportByPrefix(String s) {
      Iterator var2 = this.imports.iterator();

      Import im;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         im = (Import)var2.next();
      } while(!im.getPrefix().getArgStr().equals(s));

      return im;
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.getEffectiveMetaStatements());
      statements.addAll(this.getEffectiveLinkageStatement());
      statements.addAll(this.getEffectiveDefinitionStatement());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }

   public List<YangStatement> getEffectiveMetaStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.yangVersion != null) {
         statements.add(this.yangVersion);
      } else {
         YangVersion newYangVersion = new YangVersionImpl("1");
         newYangVersion.setContext(new YangContext(this.getContext()));
         newYangVersion.setElementPosition(this.getElementPosition());
         statements.add(newYangVersion);
      }

      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      if (this.organization != null) {
         statements.add(this.organization);
      }

      if (this.contact != null) {
         statements.add(this.contact);
      }

      statements.addAll(this.revisions);
      return statements;
   }

   public List<YangStatement> getEffectiveLinkageStatement() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.imports);
      statements.addAll(this.includes);
      return statements;
   }

   public List<YangStatement> getEffectiveDefinitionStatement() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.getContext().getExtensionCache().values());
      statements.addAll(this.getContext().getFeatureCache().values());
      statements.addAll(this.getContext().getIdentityCache().values());
      statements.addAll(this.getContext().getTypedefIdentifierCache().values());
      statements.addAll(this.getContext().getGroupingIdentifierCache().values());
      statements.addAll(this.getContext().getSchemaNodeIdentifierCache().values());
      statements.addAll(this.augments);
      return statements;
   }
}
