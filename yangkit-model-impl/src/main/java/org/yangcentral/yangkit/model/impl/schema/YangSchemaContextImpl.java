package org.yangcentral.yangkit.model.impl.schema;

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
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
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
   private SchemaNodeContainer schemaNodeContainer = new SchemaNodeContainerImpl(null);

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
            iterator = this.getSchemaNodeChildren().iterator();

            while(iterator.hasNext()) {
               SchemaNode schemaNode = (SchemaNode)iterator.next();
               if (schemaNode.getContext().getCurModule() == matchedModule) {
                  this.removeSchemaNodeChild(schemaNode);
               }
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

   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      List<Module> modules = new ArrayList();
      modules.addAll(this.getModules());
      modules.addAll(this.getImportOnlyModules());
      Iterator iterator = modules.iterator();

      Module module;
      ValidatorResult result;
      while(iterator.hasNext()) {
         module = (Module)iterator.next();
         if (!module.isInit()) {
            module.setContext(new YangContext(this, module));
            result = module.init();
            validatorResultBuilder.merge(result);
         }
      }

      iterator = modules.iterator();

      while(true) {
         do {
            do {
               do {
                  if (!iterator.hasNext()) {
                     iterator = modules.iterator();

                     while(iterator.hasNext()) {
                        module = (Module)iterator.next();
                        if (module.isInit() && module.isBuilt()) {
                           result = module.validate();
                           validatorResultBuilder.merge(result);
                           validatorResultBuilder.merge(module.afterValidate());
                        }
                     }

                     ValidatorResult validatorResult = validatorResultBuilder.build();
                     return validatorResult;
                  }

                  module = (Module)iterator.next();
               } while(!module.isInit());
            } while(module.isBuilt());
         } while(module instanceof SubModule && module.getContext().getYangSpecification() == YangSpecification.getVersion11Spec());

         result = module.build();
         validatorResultBuilder.merge(result);
      }
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
