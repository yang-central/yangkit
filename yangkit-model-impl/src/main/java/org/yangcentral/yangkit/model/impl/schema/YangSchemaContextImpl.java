package org.yangcentral.yangkit.model.impl.schema;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangSpecification;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.YangSchema;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.impl.stmt.SchemaNodeContainerImpl;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YangSchemaContextImpl implements YangSchemaContext {
   private List<Module> modules = new ArrayList<>();
   private List<Module> importOnlyModules = new ArrayList<>();
   private Map<String, List<Module>> moduleMap = new ConcurrentHashMap<>();
   private Map<String, List<YangElement>> parseResult = new ConcurrentHashMap<>();
   private YangSchema schema;

   private ValidatorResult validatorResult;
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(null);

   public List<Module> getModules() {
      return this.modules;
   }

   public List<Module> getImportOnlyModules() {
      return this.importOnlyModules;
   }

   public YangSchemaContextImpl(YangSchema schema) {
      this.schema = schema;
   }

   public YangSchemaContextImpl() {
   }


   public Optional<Module> getModule(String name, String revision) {
      return this.getModule(new ModuleId(name, revision));
   }

   public Optional<Module> getModule(ModuleId moduleId) {
      if (!this.moduleMap.containsKey(moduleId.getModuleName())) {
         return Optional.empty();
      }
      if(moduleId.getRevision() == null || moduleId.getRevision().equals("")){
         return getLatestModule(moduleId.getModuleName());
      }
      List<Module> modules = getModule(moduleId.getModuleName());
      for (Module module:modules) {
         if(module.getModuleId().equals(moduleId)){
            return Optional.of(module);
         }
      }
      return Optional.empty();
   }

   public List<Module> getModule(String name) {
      return this.moduleMap.get(name);
   }

   @Override
   public Optional<Module> getLatestModule(String name) {
      List<Module> modules = getModule(name);
      if(modules == null || modules.isEmpty()){
         return Optional.empty();
      }
      Module latestModule = null;
      for(Module module:modules){
         if(latestModule == null){
            latestModule = module;
            continue;
         }
         if(!latestModule.getCurRevisionDate().isPresent()){
            latestModule = module;
            continue;
         }
         if(!module.getCurRevisionDate().isPresent()){
            continue;
         }

         if(module.getCurRevisionDate().get().compareTo(latestModule.getCurRevisionDate().get())>0){
            latestModule = module;
         }

      }
      return Optional.of(latestModule);
   }

   public List<Module> getModule(URI namespace) {
      List<Module> candidate = new ArrayList<>();
      Iterator<List<Module>> valueIt = this.moduleMap.values().iterator();

      while(true) {
         List<Module> modules;
         do {
            if (!valueIt.hasNext()) {
               return candidate;
            }

            modules = valueIt.next();
         } while(null == modules);

         for (Module module : modules) {
            if (module.getMainModule() != null) {
               URI uri = module.getMainModule().getNamespace().getUri();
               if (uri.equals(namespace)) {
                  candidate.add(module);
               }
            }
         }
      }
   }

   public Optional<? extends SchemaNode> getSchemaNode(SchemaPath.Absolute path) {
      SchemaNode schemaNode = path.getSchemaNode(this);
      return null == schemaNode ? Optional.empty() : Optional.of(schemaNode);
   }

   @Override
   public SchemaNode getSchemaNode(AbsolutePath absolutePath) {
      if(absolutePath == null || absolutePath.isRootPath()){
         return null;
      }
      SchemaNodeContainer schemaNodeContainer = this;
      int size = absolutePath.getSteps().size();
      for(int i=0; i < size; i++){
         XPathStep step = absolutePath.getSteps().get(i);
         QName stepName = step.getStep();
         SchemaNode schemaNode = schemaNodeContainer.getSchemaNodeChild(stepName);
         if(schemaNode == null){
            break;
         }
         if(i == (size-1)){
            return schemaNode;
         }
         if(schemaNode instanceof SchemaNodeContainer){
            schemaNodeContainer = (SchemaNodeContainer) schemaNode;
         } else {
            break;
         }
      }

      return null;
   }

   public YangSchema getYangSchema() {
      return this.schema;
   }

   public void addModule(Module module) {
      if(getModule(module.getModuleId()).isPresent()) {
         Module matchedModule = getModule(module.getModuleId()).get();
         if(isImportOnly(matchedModule)){
            removeModule(matchedModule.getModuleId());
         } else {
            return;
         }
      }
      List<Module> filterModules = this.moduleMap.get(module.getArgStr());
      if(filterModules == null || filterModules.isEmpty()) {
         filterModules = new ArrayList<>();
         this.moduleMap.put(module.getArgStr(), filterModules);
      }
      filterModules.add(module);
      this.modules.add(module);
   }

   public void addImportOnlyModule(Module module) {
      if(getModule(module.getModuleId()).isPresent()){
         return;
      }
      List<Module> filterModules = this.moduleMap.get(module.getArgStr());
      if(filterModules == null || filterModules.isEmpty()) {
         filterModules = new ArrayList<>();
         this.moduleMap.put(module.getArgStr(), filterModules);
      }
      filterModules.add(module);
      importOnlyModules.add(module);
   }

   public boolean isImportOnly(Module module) {
      Iterator<Module> iterator = this.importOnlyModules.iterator();

      Module importOnlyModule;
      do {
         if (!iterator.hasNext()) {
            return false;
         }

         importOnlyModule = iterator.next();
      } while(importOnlyModule != module);

      return true;
   }

   public Module removeModule(ModuleId moduleId) {
      if (!this.moduleMap.containsKey(moduleId.getModuleName())) {
         return null;
      } else {
         List<Module> filterModules = this.moduleMap.get(moduleId.getModuleName());
         Module matchedModule = null;

         for (Module module : filterModules) {
            if (module.getModuleId().equals(moduleId)) {
               matchedModule = module;
               break;
            }
         }

         if (matchedModule == null) {
            return null;
         } else {
            filterModules.remove(matchedModule);
            for(SchemaNode schemaNode:matchedModule.getSchemaNodeChildren()){
               this.removeSchemaNodeChild(schemaNode);
            }
            this.modules.remove(matchedModule);
            this.importOnlyModules.remove(matchedModule);
            return matchedModule;
         }
      }
   }

   public Map<String, List<YangElement>> getParseResult() {
      return this.parseResult;
   }

   public SchemaNode getMandatoryDescendant() {
      return this.schemaNodeContainer.getMandatoryDescendant();
   }

   private void buildDependencies(){
      List<Module> modules = new ArrayList<>();
      modules.addAll(this.getModules());
      modules.addAll(this.getImportOnlyModules());
      for(Module module:modules){
         if(module == null){
            continue;
         }
         module.getDependentBys().clear();
      }
      for(Module module:modules){
         if(module == null){
            continue;
         }
         List<YangStatement> imports = module.getSubStatement(YangBuiltinKeyword.IMPORT.getQName());
         for(YangStatement statement:imports){
            Import im = (Import) statement;
            String moduleName = im.getArgStr();
            String revisionDate = null;
            List<YangStatement> revisionDates = im.getSubStatement(YangBuiltinKeyword.REVISIONDATE.getQName());
            if(!revisionDates.isEmpty()){
               revisionDate = revisionDates.get(0).getArgStr();
            }

            Optional<Module> importModuleOp = this.getModule(moduleName,revisionDate);
            if(importModuleOp.isPresent()){
               importModuleOp.get().addDependentBy(module);
            }
         }
      }
   }

   private void clear(Module module){
      try {
         module.clear();
      } catch (RuntimeException e){
         System.out.println("clear module:"+ module.getArgStr() + " failed. detail:"+e.getMessage());
      }



   }
   /**
    * validate yang schema context, it will initial all statements (init fields from argument and sub statements),
    * and build all init statements(build linkage,check static grammar,build schema tree,etc.), and then it will validate
    * all statements of all modules(in this phase, it will only check whether the relation between statements is correct,
    * for example, check whether the xpath expression is correct.)
    * @return the result of validation
    */
   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      List<Module> modules = new ArrayList<>();
      modules.addAll(this.getModules());
      modules.addAll(this.getImportOnlyModules());
      buildDependencies();
      for(Module module:modules){
         if(module.changed()){
            clear(module);
         }
      }
      //init
      for(Module module:modules){
         if(module.getContext() == null){
            module.setContext(new YangContext(this,module));
         }
         ValidatorResult result = module.init();
         validatorResultBuilder.merge(result);
      }

      //build
      for(Module module:modules){
         if((module instanceof SubModule)
                 && (module.getContext().getYangSpecification() == YangSpecification.getVersion11Spec())){
            //yang1.1, a submodule MUST be built by the module it belongs to.
            continue;
         }
         ValidatorResult result = module.build();
         validatorResultBuilder.merge(result);
      }
      //validate
      for(Module module:modules){
         validatorResultBuilder.merge(module.validate());
         validatorResultBuilder.merge(module.afterValidate());
      }
      ValidatorResult validatorResult = validatorResultBuilder.build();
      this.validatorResult = validatorResult;
      return validatorResult;
   }

   public ValidatorResult getValidateResult() {
      return validatorResult;
   }

   public List<SchemaNode> getSchemaNodeChildren() {
      return this.schemaNodeContainer.getSchemaNodeChildren();
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      return this.schemaNodeContainer.addSchemaNodeChild(schemaNode);
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

      for (SchemaNode schemaNode : schemaNodes) {
         validatorResultBuilder.merge(this.addSchemaNodeChild(schemaNode));
      }

      return validatorResultBuilder.build();
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
   public List<SchemaNode> getTreeNodeChildren() {
      return schemaNodeContainer.getTreeNodeChildren();
   }

   @Override
   public SchemaNode getTreeNodeChild(QName identifier) {
      return schemaNodeContainer.getTreeNodeChild(identifier);
   }

   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
   }

   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren() {
      return schemaNodeContainer.getEffectiveSchemaNodeChildren(true);
   }
   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }
}
