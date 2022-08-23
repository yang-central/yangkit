package org.yangcentral.yangkit.model.impl.schema;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangSpecification;
import org.yangcentral.yangkit.common.api.QName;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class YangSchemaContextImpl implements YangSchemaContext {
   private List<Module> modules = new ArrayList();
   private List<Module> importOnlyModules = new ArrayList();
   private Map<String, List<Module>> moduleMap = new ConcurrentHashMap();
   private Map<String, List<YangElement>> parseResult = new ConcurrentHashMap();
   private YangSchema schema;
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
      } else {
         List<Module> matched = (List)this.moduleMap.get(moduleId.getModuleName());
         Iterator iterator = matched.iterator();

         while(iterator.hasNext()) {
            Module module = (Module)iterator.next();
            if (moduleId.getRevision() == null) {
               if (module.getArgStr().equals(moduleId.getModuleName())) {
                  return Optional.of(module);
               }
            } else if (module.getModuleId().equals(moduleId)) {
               return Optional.of(module);
            }
         }

         return Optional.empty();
      }
   }

   public List<Module> getModule(String name) {
      return (List)this.moduleMap.get(name);
   }

   public List<Module> getModule(URI namespace) {
      List<Module> candiate = new ArrayList();
      Iterator<List<Module>> valueIt = this.moduleMap.values().iterator();

      while(true) {
         List modules;
         do {
            if (!valueIt.hasNext()) {
               return candiate;
            }

            modules = valueIt.next();
         } while(null == modules);

         Iterator iterator = modules.iterator();

         while(iterator.hasNext()) {
            Module module = (Module)iterator.next();
            if (module.getMainModule() != null) {
               URI uri = module.getMainModule().getNamespace().getUri();
               if (uri.equals(namespace)) {
                  candiate.add(module);
               }
            }
         }
      }
   }

   public Optional<? extends SchemaNode> getSchemaNode(SchemaPath.Absolute path) {
      SchemaNode schemaNode = path.getSchemaNode(this);
      return null == schemaNode ? Optional.empty() : Optional.of(schemaNode);
   }

   public YangSchema getYangSchema() {
      return this.schema;
   }

   public void addModule(Module module) {
      List<Module> filterModules = this.moduleMap.get(module.getArgStr());
      if (filterModules != null && filterModules.size() != 0) {
         filterModules.add(module);
      } else {
         filterModules = new ArrayList();
         filterModules.add(module);
         this.moduleMap.put(module.getArgStr(), filterModules);
      }

      this.modules.add(module);
   }

   public void addImportOnlyModule(Module module) {
      List<Module> filterModules = this.moduleMap.get(module.getArgStr());
      if (filterModules != null && filterModules.size() != 0) {
         Iterator iterator = filterModules.iterator();

         Module candidate;
         do {
            if (!iterator.hasNext()) {
               filterModules.add(module);
               this.importOnlyModules.add(module);
               return;
            }

            candidate = (Module)iterator.next();
         } while(!candidate.getModuleId().equals(module.getModuleId()));

      } else {
         filterModules = new ArrayList();
         filterModules.add(module);
         this.moduleMap.put(module.getArgStr(), filterModules);
         this.importOnlyModules.add(module);
      }
   }

   public boolean isImportOnly(Module module) {
      Iterator iterator = this.importOnlyModules.iterator();

      Module importOnlyModule;
      do {
         if (!iterator.hasNext()) {
            return false;
         }

         importOnlyModule = (Module)iterator.next();
      } while(importOnlyModule != module);

      return true;
   }

   public Module removeModule(ModuleId moduleId) {
      if (!this.moduleMap.containsKey(moduleId.getModuleName())) {
         return null;
      } else {
         List<Module> filterModules = (List)this.moduleMap.get(moduleId.getModuleName());
         Module matchedModule = null;
         Iterator iterator = filterModules.iterator();

         while(iterator.hasNext()) {
            Module module = (Module)iterator.next();
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
      List<Module> modules = new ArrayList();
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
            if(!revisionDate.isEmpty()){
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
      module.clear();
      for(Module dependent:module.getDependentBys()){
         clear(dependent);
      }
   }
   /**
    * validate yang schema context, it will initial all statements (init fields from argument and sub statements),
    * and build all init statements(build linkage,check static grammar,build schema tree,etc.), and then it will validate
    * all statements of all modules(in this phase, it will only check whether the relation between statements is correct,
    * for example, check whether the xpath expression is correct.)
    * @return
    */
   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      List<Module> modules = new ArrayList();
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
      return validatorResultBuilder.build();
   }

   public List<SchemaNode> getSchemaNodeChildren() {
      return this.schemaNodeContainer.getSchemaNodeChildren();
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      return this.isImportOnly(schemaNode.getContext().getCurModule()) ? (new ValidatorResultBuilder()).build() : this.schemaNodeContainer.addSchemaNodeChild(schemaNode);
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator iterator = schemaNodes.iterator();

      while(iterator.hasNext()) {
         SchemaNode schemaNode = (SchemaNode)iterator.next();
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

   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }
}
